package com.littlh.palelullaby;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 调试命令，用于在游戏内预览地物生成：
 * /pale_lullaby gothic_cliff  在当前所在格强制生成一座哥特悬崖（无视稀有度和群系门控）
 * /pale_lullaby place <feature>  在玩家位置放置任意 configured feature
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID)
public class PaleLullabyCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("pale_lullaby")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("gothic_cliff")
                                .executes(context -> forceGothicCliff(context.getSource())))
                        .then(Commands.literal("place")
                                .then(Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE))
                                        .executes(context -> placeFeature(context.getSource(),
                                                ResourceKeyArgument.getConfiguredFeature(context, "feature"))))));
    }

    private static int forceGothicCliff(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        Holder.Reference<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE,
                        ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "gothic_cliff")));
        ConfiguredFeature<?, ?> configured = holder.value();
        GothicCliffFeature.FORCE = true;
        boolean ok;
        try {
            ok = configured.place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
        } finally {
            GothicCliffFeature.FORCE = false;
        }
        source.sendSuccess(() -> Component.literal("gothic cliff placed: " + ok), true);
        return ok ? 1 : 0;
    }

    private static int placeFeature(CommandSourceStack source, Holder.Reference<ConfiguredFeature<?, ?>> holder) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        boolean ok = holder.value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
        source.sendSuccess(() -> Component.literal("feature placed: " + ok), true);
        return ok ? 1 : 0;
    }
}