package com.littlh.palelullaby.client;

import com.littlh.palelullaby.client.renderer.BloodLordRenderer;
import com.littlh.palelullaby.client.renderer.BloodNobleRenderer;
import com.littlh.palelullaby.client.renderer.SpellCastingBloodHunterRenderer;
import com.littlh.palelullaby.client.renderer.SpellCastingVampireRenderer;
import com.littlh.palelullaby.entity.BloodLordEntity;
import com.littlh.palelullaby.entity.BloodNobleEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.SpellCastingBloodHunterEntity;
import com.littlh.palelullaby.entity.SpellCastingVampireEntity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 铁魔法客户端渲染注册，仅在铁魔法存在时被调用。
 */
public final class IronSpellsClientCompat {
    private IronSpellsClientCompat() {
    }

    public static void registerIronSpellRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType<SpellCastingVampireEntity>) PaleLullabyEntities.VAMPIRE.get(), SpellCastingVampireRenderer::new);
        event.registerEntityRenderer((EntityType<BloodNobleEntity>) PaleLullabyEntities.BLOOD_NOBLE.get(), BloodNobleRenderer::new);
        event.registerEntityRenderer((EntityType<BloodLordEntity>) PaleLullabyEntities.BLOOD_LORD.get(), BloodLordRenderer::new);
        event.registerEntityRenderer((EntityType<SpellCastingBloodHunterEntity>) PaleLullabyEntities.BLOOD_HUNTER.get(), SpellCastingBloodHunterRenderer::new);
        event.registerEntityRenderer((EntityType<SpellCastingBloodHunterEntity>) PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), SpellCastingBloodHunterRenderer::new);
        event.registerEntityRenderer((EntityType<SpellCastingBloodHunterEntity>) PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), SpellCastingBloodHunterRenderer::new);
    }
}
