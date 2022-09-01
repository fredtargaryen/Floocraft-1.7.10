package com.fredtargaryen.floocraft.network;

import com.fredtargaryen.floocraft.DataReference;
import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.network.messages.MessageFireplaceList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class FloocraftWorldData extends SavedData {

	public final ConcurrentHashMap<String, int[]> placeList = new ConcurrentHashMap<>();

	public FloocraftWorldData() {}

	public static FloocraftWorldData load(CompoundTag nbt) {
		FloocraftWorldData data = new FloocraftWorldData();
		ListTag list = nbt.getList(DataReference.MODID, 10);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag nbt1 = list.getCompound(i);
			int[] coords = new int[]{nbt1.getInt("X"), nbt1.getInt("Y"), nbt1.getInt("Z")};
			data.placeList.put(nbt1.getString("NAME"), coords);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag compound) {
		ListTag list = new ListTag();
		for(String nextName : this.placeList.keySet()) {
			CompoundTag nbt1 = new CompoundTag();
			nbt1.putString("NAME", nextName);
			int[] coords = this.placeList.get(nextName);
			nbt1.putInt("X", coords[0]);
			nbt1.putInt("Y", coords[1]);
			nbt1.putInt("Z", coords[2]);
			list.add(nbt1);
		}
		compound.put(DataReference.MODID, list);
		return compound;
	}
	
	public static FloocraftWorldData forLevel(Level level) {
		ServerLevel serverWorld = level.getServer().getLevel(level.dimension());
		DimensionDataStorage storage = serverWorld.getDataStorage();
		return storage.computeIfAbsent(FloocraftWorldData::load, FloocraftWorldData::new, DataReference.DATA_FILE_NAME);
	}

	public void addLocation(String name, BlockPos pos) {
		placeList.put(name, new int[]{pos.getX(), pos.getY(), pos.getZ()});
		FloocraftBase.info("[FLOOCRAFT-SERVER] Added fireplace at " + pos.toString() + ". Name: " + name);
		this.setDirty();
	}
	
	public void removeLocation(int x, int y, int z) {
		int[] coords = new int[]{x, y, z};
		boolean removedPlace = false;
		Iterator i = this.placeList.keySet().iterator();
		while(i.hasNext() && !removedPlace)
		{
			String nextPlaceName = (String)i.next();
			if(Arrays.equals(this.placeList.get(nextPlaceName), coords))
			{
				FloocraftBase.info("[FLOOCRAFT-SERVER] Removed fireplace at (" + x + ", " + y + ", " + z + "). Name: " + nextPlaceName);
				this.placeList.remove(nextPlaceName);
				removedPlace = true;
			}
		}
		if(!removedPlace)
		{
            FloocraftBase.warn("[FLOOCRAFT-SERVER] Failed to remove fireplace at (" + x + ", " + y + ", " + z + ").");
			FloocraftBase.warn("[FLOOCRAFT-SERVER] Data can be manually removed with an NBT editor.");
		}
		this.setDirty();
	}
	
	public MessageFireplaceList assembleNewFireplaceList(Level l) {
		MessageFireplaceList m = new MessageFireplaceList();
		m.places = this.placeList.keySet().toArray();
		boolean[] list = new boolean[m.places.length];
		int keyCount = 0;
		FlooFlamesBase greenTemp = (FlooFlamesBase) FloocraftBase.GREEN_FLAMES_TEMP.get();
		for(String nextName : this.placeList.keySet()) {
			int[] coords = this.placeList.get(nextName);
            BlockPos dest = new BlockPos(coords[0], coords[1], coords[2]);
			BlockState state = l.getBlockState(dest);
            boolean ok;
            if(state.is(BlockTags.FIRE)) {
                ok = greenTemp.isInFireplace(l, dest) != null;
            }
			else {
				ok = state.getBlock() instanceof FlooFlamesBase;
			}
            list[keyCount] = ok;
			++keyCount;
		}
		m.enabledList = list;
		return m;
	}
}