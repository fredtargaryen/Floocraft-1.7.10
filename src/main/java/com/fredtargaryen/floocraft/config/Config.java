package com.fredtargaryen.floocraft.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.nio.file.Path;

/**
 * Replaces the Configuration system of Forge 1.12.2.
 */
@Mod.EventBusSubscriber
public class Config {
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;
    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    static {
        ClientConfig.init(CLIENT_BUILDER);
        CommonConfig.init(COMMON_BUILDER);
        CLIENT_CONFIG_SPEC = CLIENT_BUILDER.build();
        COMMON_CONFIG_SPEC = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading loadEvent) {}

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {}
}
