package com.fredtargaryen.floocraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * ===DESCRIPTION OF MESSAGE CHANNELS===
 * MessageApproval: sends to client whether proposed fireplace name is valid (i.e. if the sign is decorative OR the name has not been used already). If approved the server will take care of adding the fireplace
 * MessageApproveFireplace: sends fireplace sign position, proposed fireplace name, and whether the sign is decorative or connected to the Network, to server for approval.
 * MessageDoGreenFlash: causes the green/magenta flash effect to appear on the client when you teleport
 * MessageEndPeek: sent when the "Mischief managed" button is pressed to stop the peek
 * MessageFireplaceList: the lists of fireplace attributes: names; x coordinates; y coordinates; z coordinates; whether they can be teleported to
 * MessageFireplaceListRequest: empty packet. When this is received, server sends back MessageFireplaceList
 * MessageFlooTorchTeleport: sent when a client player touches a Floo Torch and causes a random teleport. This can't be done via the server only; this just results in "Player moved too quickly!"
 * MessagePeekRequest: sent to the server when the player presses the "Peek..." button
 * MessagePlayerID: sends the unique IDs of a Peeker and a player, so that the Peeker can have the texture of the player's head
 * MessagePlayerIDRequest: sent by the client to retrieve the ID of a Peeker's corresponding player
 * MessageStartPeek: sent by the client when initiating a peek
 * MessageTeleportEntity: teleports player that sent this to (destX, destY, destZ) if possible
 *
 * When changing version number, change in: build.gradle, mods.toml
 */

public class DataReference {
    public static final String MODID = "floocraftft";
    public static final String DATA_FILE_NAME = MODID + ".dat";

    public static final int FLOO_FIRE_DETECTION_RANGE = 8;

    public static final int POT_MIN_H_RANGE = 2;
    public static final int POT_MAX_H_RANGE = 5;
    public static final int POT_MIN_V_RANGE = 2;
    public static final int POT_MAX_V_RANGE = 5;

    // GUI textures
    public static final ResourceLocation SIGN_TEX_LOC = new ResourceLocation(MODID, "blocks/floosign");
    public static final ResourceLocation TP_BACKGROUND = new ResourceLocation(MODID, "textures/blocks/tp_background.png");

    // Custom tags
    public static final ResourceLocation VALID_ARRIVAL_BLOCKS = new ResourceLocation(MODID, "valid_arrival_blocks");
    public static final ResourceLocation VALID_DEPARTURE_BLOCKS = new ResourceLocation(MODID, "valid_departure_blocks");
    public static final TagKey<Block> VALID_ARRIVAL_BLOCKS_TAG = BlockTags.create(VALID_ARRIVAL_BLOCKS);
    public static final TagKey<Block> VALID_DEPARTURE_BLOCKS_TAG = BlockTags.create(VALID_DEPARTURE_BLOCKS);

    public static final ResourceLocation GREENED_RL = new ResourceLocation(MODID, "greened");
    public static final ResourceLocation TP_RL = new ResourceLocation(MODID, "tp");
    public static final ResourceLocation FLICK_RL = new ResourceLocation(MODID, "flick");
}
