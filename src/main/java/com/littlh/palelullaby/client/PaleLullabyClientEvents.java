package com.littlh.palelullaby.client;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.PaleLullabyBiomes;
import com.littlh.palelullaby.PaleLullabyBlocks;
import com.littlh.palelullaby.PaleLullabyCompat;
import com.littlh.palelullaby.PaleLullabyEffects;
import com.littlh.palelullaby.PaleLullabyItems;
import com.littlh.palelullaby.PaleLullabyParticles;
import com.littlh.palelullaby.client.particle.CrimsonRoseSparkParticle;
import com.littlh.palelullaby.client.renderer.BloodHunterRenderer;
import com.littlh.palelullaby.client.renderer.BrokenSwordBlockEntityRenderer;
import com.littlh.palelullaby.client.renderer.DriedBloodGhostRenderer;
import com.littlh.palelullaby.client.renderer.FallenBloodHunterRenderer;
import com.littlh.palelullaby.client.renderer.GhostWalkLayer;
import com.littlh.palelullaby.client.renderer.IronMaidenBlockEntityRenderer;
import com.littlh.palelullaby.client.renderer.LordVampireRenderer;
import com.littlh.palelullaby.client.renderer.MobSkins;
import com.littlh.palelullaby.client.renderer.MullandRenderer;
import com.littlh.palelullaby.client.renderer.NobleVampireRenderer;
import com.littlh.palelullaby.client.renderer.PaleMinionRenderer;
import com.littlh.palelullaby.client.model.TolandBatModel;
import com.littlh.palelullaby.client.renderer.VampireRenderer;
import com.littlh.palelullaby.client.renderer.TolandBatRenderer;
import com.littlh.palelullaby.entity.BloodHunterEntity;
import com.littlh.palelullaby.entity.LordVampireEntity;
import com.littlh.palelullaby.entity.NobleVampireEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.TolandBatEntity;
import com.littlh.palelullaby.entity.VampireEntity;
import com.littlh.palelullaby.fluid.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PaleLullabyClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PaleLullabyEntities.MULLAND.get(), MullandRenderer::new);
        event.registerEntityRenderer(PaleLullabyEntities.PALE_MINION.get(), PaleMinionRenderer::new);
        event.registerEntityRenderer(PaleLullabyEntities.FALLEN_BLOOD_HUNTER.get(), FallenBloodHunterRenderer::new);
        event.registerEntityRenderer(PaleLullabyEntities.DRIED_BLOOD_GHOST.get(), DriedBloodGhostRenderer::new);
        event.registerEntityRenderer(PaleLullabyEntities.TOLAND_BAT.get(), TolandBatRenderer::new);
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            IronSpellsClientCompat.registerIronSpellRenderers(event);
        } else {
            registerVanillaRenderers(event);
        }
        event.registerEntityRenderer(PaleLullabyEntities.BLOOD_NEEDLE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(PaleLullabyEntities.SILVER_BOLT.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(PaleLullabyBlocks.IRON_MAIDEN_BE.get(), IronMaidenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PaleLullabyBlocks.BROKEN_SWORD_BE.get(), BrokenSwordBlockEntityRenderer::new);
    }

    private static void registerVanillaRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType<VampireEntity>) PaleLullabyEntities.VAMPIRE.get(), VampireRenderer::new);
        event.registerEntityRenderer((EntityType<NobleVampireEntity>) PaleLullabyEntities.BLOOD_NOBLE.get(), NobleVampireRenderer::new);
        event.registerEntityRenderer((EntityType<LordVampireEntity>) PaleLullabyEntities.BLOOD_LORD.get(), LordVampireRenderer::new);
        event.registerEntityRenderer((EntityType<BloodHunterEntity>) PaleLullabyEntities.BLOOD_HUNTER.get(), BloodHunterRenderer::new);
        event.registerEntityRenderer((EntityType<BloodHunterEntity>) PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), BloodHunterRenderer::new);
        event.registerEntityRenderer((EntityType<BloodHunterEntity>) PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), BloodHunterRenderer::new);
    }


    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        // 幽体漫步：给玩家渲染器叠加半透明身体层
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer wideRenderer) {
            wideRenderer.addLayer(new GhostWalkLayer(wideRenderer));
        }
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer slimRenderer) {
            slimRenderer.addLayer(new GhostWalkLayer(slimRenderer));
        }
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        // 资源重载时清空皮肤库缓存，重新扫描贴图文件夹
        event.registerReloadListener(MobSkins.instance());
    }
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TolandBatModel.LAYER_LOCATION, TolandBatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PaleLullabyParticles.CRIMSON_ROSE_SPARK.get(), CrimsonRoseSparkParticle.Provider::new);
    }

    private static final ResourceLocation BLOOD_FRENZY_ICON =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/mob_effect/blood_frenzy.png");
    private static final ResourceLocation BLOOD_THIRST_ICON =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/mob_effect/blood_thirst.png");

    @SubscribeEvent
    public static void registerMobEffectExtensions(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(effectIcon(BLOOD_FRENZY_ICON), PaleLullabyEffects.BLOOD_FRENZY.get());
        event.registerMobEffect(effectIcon(BLOOD_THIRST_ICON), PaleLullabyEffects.BLOOD_THIRST.get());
    }

    private static IClientMobEffectExtensions effectIcon(ResourceLocation icon) {
        return new IClientMobEffectExtensions() {
            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics,
                                         int x, int y, float z, float alpha) {
                graphics.blit(icon, x, y, 0.0F, 0.0F, 18, 18, 18, 18);
                return true;
            }

            @Override
            public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
                                               GuiGraphics graphics, int x, int y, int z) {
                graphics.blit(icon, x, y, 0.0F, 0.0F, 18, 18, 18, 18);
                return true;
            }
        };
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.GRAY_MATTER_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.GRAY_MATTER_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.BLOOD_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.BLOOD_FLOWING.get(), RenderType.translucent());

            // Render transparent blocks with cutout so see-through parts do not show as black.
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.BONE_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.RED_NEEDLE_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.PALE_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.RED_NEEDLE_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.BLIND_FLOSS_FLOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.CRIMSON_THORN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.CRIMSON_ROSE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.PALE_WHEAT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(PaleLullabyBlocks.WITHERED_MISTLETOE.get(), RenderType.cutout());
        });
    }
}