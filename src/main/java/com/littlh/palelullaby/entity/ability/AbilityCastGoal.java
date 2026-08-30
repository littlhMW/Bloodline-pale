package com.littlh.palelullaby.entity.ability;

import com.littlh.palelullaby.entity.CasterMob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用施法目标：按“距离 + 双方血量”加权选择能力，站定读条（只占 LOOK），
 * 读条结束执行能力并进入该能力的独立冷却。血步读条期间施法者无敌。
 */
public class AbilityCastGoal<T extends PathfinderMob & CasterMob> extends Goal {
    private final T mob;
    private final List<BloodAbility> abilities;
    private final Map<ResourceLocation, Integer> cooldowns = new HashMap<>();
    private int castTicks = -1;
    private BloodAbility casting;

    public AbilityCastGoal(T mob, List<BloodAbility> abilities) {
        this.mob = mob;
        this.abilities = abilities;
        // 只声明 LOOK，风筝走位（MOVE）在读条时仍然生效。
        this.setFlags(java.util.EnumSet.of(Goal.Flag.LOOK));
    }

    /** range <= 0 表示对自身施放（净化/增益），永远可用。 */
    private boolean inRange(BloodAbility ability, LivingEntity target) {
        return ability.range() <= 0.0D || this.mob.distanceToSqr(target) <= ability.range() * ability.range();
    }

    @Override
    public boolean canUse() {
        if (this.mob.isCasting()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return this.abilities.stream().anyMatch(a ->
                this.ready(a) && this.inRange(a, target) && this.mob.canCastAbility(a, target));
    }

    @Override
    public boolean canContinueToUse() {
        return this.castTicks >= 0;
    }

    private boolean ready(BloodAbility ability) {
        return this.cooldowns.getOrDefault(ability.id(), 0) <= 0;
    }

    @Override
    public void start() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            this.stop();
            return;
        }
        List<BloodAbility> usable = new ArrayList<>();
        for (BloodAbility a : this.abilities) {
            if (this.ready(a) && this.inRange(a, target) && this.mob.canCastAbility(a, target)) {
                usable.add(a);
            }
        }
        if (usable.isEmpty()) {
            this.stop();
            return;
        }
        this.casting = this.pickWeighted(usable, target);
        this.castTicks = this.casting.castTime();
        this.mob.setCasting(true);
        this.mob.getNavigation().stop();
        this.mob.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F + this.mob.getRandom().nextFloat() * 0.3F);
        for (int i = 0; i < 6; i++) {
            this.mob.level().addParticle(ParticleTypes.CRIT,
                    this.mob.getX() + this.mob.getRandom().nextGaussian() * 0.6D,
                    this.mob.getEyeY() + this.mob.getRandom().nextGaussian() * 0.4D,
                    this.mob.getZ() + this.mob.getRandom().nextGaussian() * 0.6D,
                    0, 0.02D, 0);
        }
    }

    /**
     * 加权决策：
     * 距离<3格 → 近战权重×7、逃逸×3、远程禁用；
     * 3~8格 → 近战强化/短距法术权重上调；
     * 8~16格 → 远程；
     * 自身血量<30% → 逃逸权重×3、吸血权重×4；
     * 目标血量<35% → 处决权重×5。
     */
    private BloodAbility pickWeighted(List<BloodAbility> usable, LivingEntity target) {
        double dist = Math.sqrt(this.mob.distanceToSqr(target));
        double selfRatio = this.mob.getHealth() / Math.max(1.0F, this.mob.getMaxHealth());
        double targetRatio = target.getHealth() / Math.max(1.0F, target.getMaxHealth());
        int total = 0;
        List<Integer> weights = new ArrayList<>();
        for (BloodAbility a : usable) {
            int w = a.weight();
            switch (a.type()) {
                case MELEE_ARC -> {
                    if (dist < 3.0D) w *= 7;
                    else if (dist <= 8.0D) w *= 4;
                    else w = 0;
                }
                case RANGED -> {
                    if (dist < 3.0D) w = 0;
                    else if (dist >= 4.0D && dist <= 12.0D) w *= 2;
                    else if (dist > 16.0D) w = 0;
                }
                case TELEPORT -> {
                    if (selfRatio < 0.30D) w *= 3;
                    if (dist < 3.0D) w *= 3;
                }
                case LIFESTEAL -> {
                    if (selfRatio < 0.50D) w *= 4;
                    if (targetRatio < 0.30D) w = 0;
                    if (dist < 3.0D) w = 0;
                }
                case EXECUTE -> {
                    if (targetRatio < 0.35D) w *= 5;
                    else w = 1;
                }
                case DEBUFF -> {
                    if (target.hasEffect(com.littlh.palelullaby.PaleLullabyEffects.SANGUINE_PLAGUE)) w = 0;
                }
                case AOE -> {
                    if (this.enemyCountWithin(3.0D) < 2) w = 0;
                }
                default -> {
                }
            }
            w = Math.max(0, w);
            weights.add(w);
            total += w;
        }
        if (total <= 0) {
            return usable.get(this.mob.getRandom().nextInt(usable.size()));
        }
        int roll = this.mob.getRandom().nextInt(total);
        for (int i = 0; i < usable.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return usable.get(i);
            }
        }
        return usable.get(usable.size() - 1);
    }

    private int enemyCountWithin(double radius) {
        double r2 = radius * radius;
        int count = 0;
        for (net.minecraft.world.entity.Entity e : this.mob.level().getEntities(this.mob,
                this.mob.getBoundingBox().inflate(radius), e -> e instanceof LivingEntity le && le.isAlive())) {
            LivingEntity le = (LivingEntity) e;
            if (this.mob.distanceToSqr(le) <= r2
                    && com.littlh.palelullaby.entity.FactionTargets.isEnemy(this.mob, le)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void tick() {
        // 冷却随时间自然递减。
        this.cooldowns.replaceAll((id, cd) -> Math.max(0, cd - 1));
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            this.stop();
            return;
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.castTicks > 0) {
            this.castTicks--;
            this.mob.getNavigation().stop();
            // 血步读条 0.5 秒无敌帧。
            if (this.casting.type() == AbilityType.TELEPORT) {
                this.mob.invulnerableTime = Math.max(this.mob.invulnerableTime, 10);
            }
        } else {
            this.mob.performAbility(this.casting, target);
            this.mob.swing(InteractionHand.MAIN_HAND);
            int span = Math.max(1, this.casting.cooldownMax() - this.casting.cooldownMin());
            this.cooldowns.put(this.casting.id(),
                    this.casting.cooldownMin() + this.mob.getRandom().nextInt(span));
            this.stop();
        }
    }

    @Override
    public void stop() {
        this.mob.setCasting(false);
        this.castTicks = -1;
        this.casting = null;
    }
}
