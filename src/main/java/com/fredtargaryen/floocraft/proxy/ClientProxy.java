package com.fredtargaryen.floocraft.proxy;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.client.gui.Flash;
import com.fredtargaryen.floocraft.client.gui.FlooSignScreen;
import com.fredtargaryen.floocraft.client.gui.FloowerPotScreen;
import com.fredtargaryen.floocraft.client.gui.TeleportScreen;
import com.fredtargaryen.floocraft.client.ticker.OverrideTicker;
import com.fredtargaryen.floocraft.entity.PeekerEntity;
import com.fredtargaryen.floocraft.network.messages.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy implements IProxy {
    public OverrideTicker overrideTicker;
    public Flash flash;

    @Override
    public void onMessage(MessageApproval ma) {
        Screen s = Minecraft.getInstance().screen;
        if(s instanceof FlooSignScreen) {
            ((FlooSignScreen) s).dealWithAnswer(ma.answer);
        }
    }

    @Override
    public void onMessage(MessageDoGreenFlash mdgf) { this.flash.start(mdgf.soul); }

    @Override
    public void onMessage(MessageFireplaceList mfl) {
        Screen s = Minecraft.getInstance().screen;
        if(s instanceof TeleportScreen) {
            ((TeleportScreen) s).onFireplaceList(mfl);
        }
    }

    @Override
    public void onMessage(MessageStartPeek msp) {
        Screen s = Minecraft.getInstance().screen;
        if(s instanceof TeleportScreen) {
            ((TeleportScreen) s).onStartPeek(msp);
        }
    }

    @Override
    public void registerGUIs() {
        MenuScreens.register(FloocraftBase.POT_MENU_TYPE.get(), FloowerPotScreen::new);
    }

    @Override
    public void registerTickHandlers() {
        //Tickers now register and unregister themselves when necessary, improving performance very slightly
        this.overrideTicker = new OverrideTicker();
        this.flash = new Flash();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setUUIDs(MessagePlayerID message) {
        PeekerEntity ep = (PeekerEntity) FloocraftBase.proxy.getEntityWithUUID(Minecraft.getInstance().level, message.peekerUUID);
        ep.setPlayerUUID(message.playerUUID);
    }

    @Override
    public void setupRenderTypes() {
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.GREEN_FLAMES_BUSY.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.GREEN_FLAMES_IDLE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.GREEN_FLAMES_TEMP.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.MAGENTA_FLAMES_BUSY.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.MAGENTA_FLAMES_IDLE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.MAGENTA_FLAMES_TEMP.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.FLOO_CAMPFIRE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.FLOO_SOUL_CAMPFIRE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(FloocraftBase.BLOCK_FLOO_TORCH.get(), RenderType.cutoutMipped());
    }

    @Override
    public Entity getEntityWithUUID(Level level, UUID uuid) {
        if(level != null && uuid != null) {

            Iterator<Entity> iterator = level.getEntities(null, AABB.ofSize(Vec3.ZERO, 30000000, 30000000, 30000000)).iterator();
            while (iterator.hasNext()) {
                Entity next = iterator.next();
                if (next.getUUID().equals(uuid)) return next;
            }
        }
        return null;
    }

    /**
     * For texture stitching
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void stitchTextures(TextureStitchEvent.Pre tse)
    {
        TextureAtlas ta = tse.getAtlas();
        System.out.println("TODO");
//        if(tse.getAtlas().location().equals( Atlases.SIGN_ATLAS))
//        {
//            tse.addSprite(DataReference.SIGN_TEX_LOC);
//        }
    }
}
