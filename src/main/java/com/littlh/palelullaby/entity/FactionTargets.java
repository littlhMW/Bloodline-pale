package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyForgeEvents;
import com.littlh.palelullaby.PlayerFaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.littlh.palelullaby.entity.PaleLullabyFactions.HUNTER;
import static com.littlh.palelullaby.entity.PaleLullabyFactions.MAD_HUNTER;
import static com.littlh.palelullaby.entity.PaleLullabyFactions.MAD_VAMPIRE;
import static com.littlh.palelullaby.entity.PaleLullabyFactions.MULLAND;
import static com.littlh.palelullaby.entity.PaleLullabyFactions.VAMPIRE;

/**
 * 阵营仇恨的统一注册入口。每个属于本体系的怪物在 registerGoals 里调用
 * {@link #register(Mob, PaleLullabyFactions)} 即可获得：
 * - 0 优先级：受击报复（HurtByTargetGoal）；
 * - 之后按各阵营的“目标优先级”注册 NearestAttackableTargetGoal。
 *
 * 疯狂阵营（MAD_VAMPIRE / MAD_HUNTER）不攻击自身疯狂阵营：
 * 例如枯血鬼不攻击枯血鬼，但仍会攻击堕落血猎。
 */
public final class FactionTargets {
    private FactionTargets() {
    }

    /** 报复目标优先级：故意放低，让真正的敌对目标优先（数值越大优先级越低）。 */
    private static final int REVENGE_PRIORITY = 5;

    /** 为指定阵营的怪物注册全套仇恨目标。 */
    public static void register(PathfinderMob mob, PaleLullabyFactions faction) {
        mob.targetSelector.addGoal(REVENGE_PRIORITY, new FactionHurtByTargetGoal(mob, hurtByIgnore(faction), faction));
        for (Spec spec : specs(faction)) {
            mob.targetSelector.addGoal(spec.priority(),
                    new NearestAttackableTargetGoal<>(mob, spec.targetClass(), 10, spec.mustSee(), false, spec.predicate()));
        }
    }

    private record Spec(int priority, Class<? extends LivingEntity> targetClass, boolean mustSee, Predicate<LivingEntity> predicate) {
    }

    private static Spec mobSpec(int priority, Predicate<LivingEntity> predicate) {
        return new Spec(priority, Mob.class, true, predicate);
    }

    /** 无需视线（Boss 感知）。 */
    private static Spec mobSenseSpec(int priority, Predicate<LivingEntity> predicate) {
        return new Spec(priority, Mob.class, false, predicate);
    }

    private static Spec playerSpec(int priority) {
        return new Spec(priority, Player.class, true, e -> true);
    }

    private static Spec playerSpec(int priority, Predicate<Player> predicate) {
        return new Spec(priority, Player.class, true, e -> e instanceof Player p && predicate.test(p));
    }

    private static Spec playerSenseSpec(int priority) {
        return new Spec(priority, Player.class, false, e -> true);
    }

    private static Spec villagerSpec(int priority) {
        return new Spec(priority, Villager.class, true, e -> true);
    }

    private static List<Spec> specs(PaleLullabyFactions faction) {
        return switch (faction) {
            case VAMPIRE -> List.of(
                    mobSpec(1, e -> factionOf(e) == HUNTER),
                    mobSpec(2, e -> isMad(e)),
                    playerSpec(3, p -> PlayerFaction.of(p) != PlayerFaction.Faction.VAMPIRE),
                    villagerSpec(4));
            case HUNTER -> List.of(
                    mobSpec(1, e -> factionOf(e) == VAMPIRE),
                    mobSpec(2, e -> isMad(e)),
                    playerSpec(3, p -> PlayerFaction.of(p) == PlayerFaction.Faction.VAMPIRE));
            case MAD_VAMPIRE -> List.of(
                    mobSpec(1, e -> {
                        PaleLullabyFactions f = factionOf(e);
                        return f == HUNTER || f == MAD_HUNTER;
                    }),
                    playerSpec(2),
                    mobSpec(3, e -> factionOf(e) == VAMPIRE));
            case MAD_HUNTER -> List.of(
                    mobSpec(1, e -> {
                        PaleLullabyFactions f = factionOf(e);
                        return f == VAMPIRE || f == MAD_VAMPIRE;
                    }),
                    playerSpec(2),
                    mobSpec(3, e -> factionOf(e) == HUNTER));
            case MULLAND -> List.of(
                    playerSenseSpec(1),
                    mobSenseSpec(2, e -> factionOf(e) != null),
                    mobSenseSpec(3, e -> e instanceof Villager));
        };
    }

    private static PaleLullabyFactions factionOf(LivingEntity entity) {
        return PaleLullabyFactions.of(entity);
    }

    private static boolean isMad(LivingEntity entity) {
        PaleLullabyFactions f = PaleLullabyFactions.of(entity);
        return f == MAD_VAMPIRE || f == MAD_HUNTER;
    }

    /** 按仇恨总表判断两个实体是否为敌对（供 AOE 等能力使用）。 */
    public static boolean isEnemy(Mob attacker, LivingEntity target) {
        PaleLullabyFactions af = PaleLullabyFactions.of(attacker);
        PaleLullabyFactions tf = PaleLullabyFactions.of(target);
        if (af == null || attacker == target) {
            return false;
        }
        return switch (af) {
            case VAMPIRE -> tf == HUNTER || isMad(target)
                    || (target instanceof Player p && PlayerFaction.of(p) != PlayerFaction.Faction.VAMPIRE)
                    || target instanceof Villager;
            case HUNTER -> tf == VAMPIRE || isMad(target)
                    || (target instanceof Player p && PlayerFaction.of(p) == PlayerFaction.Faction.VAMPIRE);
            case MAD_VAMPIRE -> tf == HUNTER || tf == MAD_HUNTER || tf == VAMPIRE || target instanceof Player;
            case MAD_HUNTER -> tf == VAMPIRE || tf == MAD_VAMPIRE || tf == HUNTER || target instanceof Player;
            case MULLAND -> tf != null || target instanceof Player || target instanceof Villager;
        };
    }

    /** 受击报复忽略对象：同阵营玩家、以及自身疯狂阵营的同族（枯血不报复枯血）。 */
    private static Predicate<LivingEntity> hurtByIgnore(PaleLullabyFactions faction) {
        return switch (faction) {
            case VAMPIRE -> e -> false;
            case HUNTER -> e -> factionOf(e) == HUNTER;
            case MAD_VAMPIRE -> e -> factionOf(e) == MAD_VAMPIRE;
            case MAD_HUNTER -> e -> factionOf(e) == MAD_HUNTER;
            default -> e -> false;
        };
    }

    /** 受击报复：猎人不报复同阵营猎人怪物；血族/血猎被打时广播群体仇恨。友军（同阵营）伤害需“明显主动”才报复。 */
    private static class FactionHurtByTargetGoal extends HurtByTargetGoal {
        /** 一次“明显不是故意”的伤害阈值，超过即视为主动攻击。 */
        private static final float SIGNIFICANT_DAMAGE = 5.0F;
        /** 时间窗内被同一攻击者打够次数也视为主动攻击。 */
        private static final int MIN_HITS = 3;
        /** 次数统计时间窗（tick，100 = 5 秒）。 */
        private static final long HIT_WINDOW_TICKS = 100;

        private final Predicate<LivingEntity> ignore;
        private final PaleLullabyFactions faction;
        private final Map<UUID, HitRecord> hits = new HashMap<>();
        private int lastSeenTimestamp = -1;

        FactionHurtByTargetGoal(PathfinderMob mob, Predicate<LivingEntity> ignore, PaleLullabyFactions faction) {
            super(mob);
            this.ignore = ignore;
            this.faction = faction;
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = this.mob.getLastHurtByMob();
            if (attacker != null && this.ignore.test(attacker)) {
                return false;
            }
            // 友军（同阵营怪物/玩家）的轻微误伤不立刻反打，只有明显伤害或多次攻击才报复。
            if (attacker != null && this.isFriendlyHit(attacker) && !this.isSignificantHit(attacker)) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity current = this.mob.getTarget();
            LivingEntity attacker = this.mob.getLastHurtByMob();
            // A mad-faction attacker takes priority over whoever we were already
            // fighting, so the victim cannot keep ignoring the mad creature.
            if (current != null && attacker != null && current != attacker && isMad(attacker)) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            // 群体仇恨：血族与血猎被打后，把攻击者广播给附近同阵营实体。
            if (this.faction == VAMPIRE || this.faction == HUNTER) {
                this.alertSameFaction();
            }
        }

        private void alertSameFaction() {
            LivingEntity attacker = this.mob.getLastHurtByMob();
            if (attacker == null) {
                return;
            }
            double range = this.getFollowDistance();
            AABB box = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(range, 10.0, range);
            List<Mob> list = this.mob.level().getEntitiesOfClass(Mob.class, box, EntitySelector.NO_SPECTATORS);
            for (Mob other : list) {
                if (other == this.mob || other.getTarget() != null) {
                    continue;
                }
                if (PaleLullabyFactions.of(other) != this.faction) {
                    continue;
                }
                if (other.isAlliedTo(attacker)) {
                    continue;
                }
                other.setTarget(attacker);
            }
        }

        /** 攻击者是否属于我方（同阵营怪物或同阵营玩家）。 */
        private boolean isFriendlyHit(LivingEntity attacker) {
            if (attacker instanceof Player p) {
                return (this.faction == VAMPIRE && PlayerFaction.of(p) == PlayerFaction.Faction.VAMPIRE)
                        || (this.faction == HUNTER && PlayerFaction.of(p) == PlayerFaction.Faction.HUNTER);
            }
            return PaleLullabyFactions.of(attacker) == this.faction;
        }

        /** 一次实际伤害 >=5，或 5 秒内被同一攻击者打够 3 次，才算“主动攻击”。 */
        private boolean isSignificantHit(LivingEntity attacker) {
            int ts = this.mob.getLastHurtByMobTimestamp();
            long now = this.mob.level().getGameTime();
            HitRecord rec = this.hits.computeIfAbsent(attacker.getUUID(), k -> new HitRecord());
            if (ts != this.lastSeenTimestamp) {
                this.lastSeenTimestamp = ts;
                if (PaleLullabyForgeEvents.takeLastHitDamage(this.mob) >= SIGNIFICANT_DAMAGE) {
                    return true;
                }
                if (now - rec.lastHitTime > HIT_WINDOW_TICKS) {
                    rec.count = 0;
                }
                rec.count++;
                rec.lastHitTime = now;
            }
            return rec.count >= MIN_HITS;
        }

        private static class HitRecord {
            int count;
            long lastHitTime;
        }
    }
}
