package com.fredtargaryen.floocraft.client.gui;

import com.fredtargaryen.floocraft.FloocraftBase;
import com.fredtargaryen.floocraft.entity.PeekerEntity;
import com.fredtargaryen.floocraft.network.MessageHandler;
import com.fredtargaryen.floocraft.network.messages.MessageEndPeek;
import com.fredtargaryen.floocraft.proxy.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class PeekScreen extends Screen {
    private boolean peekerSpawned;
    private String fireplaceName;
    private Button doneBtn;
    private Entity player;
    private UUID peekerID;
    private boolean peekFailedOutOfRange;

    private static final TranslatableComponent PEEK_DONE = new TranslatableComponent("gui.peek.done");
    private static final TranslatableComponent OUT_OF_RANGE = new TranslatableComponent("gui.peek.outofrange");
    private static final TranslatableComponent WAITING = new TranslatableComponent("gui.peek.waiting");

    public PeekScreen(String name, UUID peekerID) {
        super(new TextComponent(name));
        this.peekerSpawned = false;
        this.fireplaceName = name;
        this.peekerID = peekerID;
        MinecraftForge.EVENT_BUS.register(this);
        this.peekFailedOutOfRange = false;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if(!this.peekerSpawned) {
            PeekerEntity ep = (PeekerEntity) FloocraftBase.proxy.getEntityWithUUID(this.minecraft.level, this.peekerID);
            if(ep == null) {
                //Give up. TODO Maybe one day someone can add peeking into chunks outside the view distance?
                this.peekFailedOutOfRange = true;
            }
            else {
                this.peekerSpawned = true;
                this.player = this.minecraft.getCameraEntity();
                this.minecraft.setCameraEntity(ep);
            }
        }
        this.doneBtn = new Button(this.width / 2 - 100, this.height - 40, 200, 20, PEEK_DONE,
                button -> {
                    PeekScreen.this.onClose();
                    Minecraft.getInstance().setScreen(null);
                });
        this.doneBtn.active = true;
        this.addRenderableWidget(this.doneBtn);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onClose() {
        ClientProxy proxy = (ClientProxy) FloocraftBase.proxy;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        MinecraftForge.EVENT_BUS.unregister(this);
        proxy.overrideTicker.start();
        this.minecraft.setCameraEntity(this.player);
        MessageEndPeek mep = new MessageEndPeek();
        mep.peekerUUID = this.peekerID;
        MessageHandler.INSTANCE.sendToServer(mep);
        super.onClose();
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    public boolean charTyped(char par1, int par2) {
        if (par2 == 1) {
            Minecraft.getInstance().setScreen(null);
        }
        return true;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack stack, int mousex, int mousey, float partialticks) {
        this.drawCenteredString(stack, this.font,
                this.peekerSpawned ?
                        new TranslatableComponent("gui.peek.peeking", this.fireplaceName) :
                        this.peekFailedOutOfRange ?
                                OUT_OF_RANGE :
                                WAITING,
                this.width / 2,
                15,
                16777215);
        super.render(stack, mousex, mousey, partialticks);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent lhe) {
        if(lhe.getEntity() == this.player) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent lde) {
        if(lde.getEntity() == this.player) {
            Minecraft.getInstance().setScreen(null);
        }
    }
}