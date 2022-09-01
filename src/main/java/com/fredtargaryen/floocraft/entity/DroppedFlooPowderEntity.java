package com.fredtargaryen.floocraft.entity;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.block.FlooFlamesBase;
import com.fredtargaryen.floocraft.block.FlooFlamesTemp;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;

public class DroppedFlooPowderEntity extends ItemEntity {
    private byte concentration;

	public DroppedFlooPowderEntity(Level level, double x, double y, double z, ItemStack stack, byte conc) {
		super(level, x, y, z, stack);
        this.concentration = conc;
	}

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("Concentration", this.concentration);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag par1) {
        super.readAdditionalSaveData(par1);
        this.concentration = par1.getByte("Concentration");
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }

    /**
     * Called when the entity is attacked.
     * TODO Add a case for campfires whenever this works again
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
            BlockPos pos = this.getOnPos();
            if (this.level.getBlockState(pos).is(BlockTags.FIRE)) {
                if(((FlooFlamesTemp)FloocraftBase.GREEN_FLAMES_TEMP.get()).isInFireplace(this.level, pos) != null) {
                    Block fireBlock = FlooFlamesBase.getFlooFireBlockToPlace(this.level, pos);
                    this.level.setBlock(pos, fireBlock.defaultBlockState().setValue(BlockStateProperties.AGE_15, (int) this.concentration), 3);
                    this.playSound(FloocraftBase.GREENED.get(), 1.0F, 1.0F);
                }
                this.remove(RemovalReason.DISCARDED);
                return true;
            }
            else
            {
                return false;
            }
        }
        return super.hurt(source, amount);
    }
}
