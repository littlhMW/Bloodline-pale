package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyItems;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * 巨蝙蝠托兰：友善的蝙蝠商人占位实体。
 * 用血痕（BLOOD_MARK）交易阵营道具：金泪滴徽章、铁露滴徽章、无辜者的舌头。
 * 交易通过原版村民式交易界面（MerchantMenu）完成。
 */
public class TolandBatEntity extends PathfinderMob implements Merchant {

    /** 单次交易需要的血痕数量 */
    public static final int BLOOD_MARK_COST = 16;

    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;

    public TolandBatEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ===== Merchant =====

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.offers.add(new MerchantOffer(
                    new ItemCost(PaleLullabyItems.BLOOD_MARK.get(), BLOOD_MARK_COST),
                    new ItemStack(PaleLullabyItems.GOLDEN_TEAR_BADGE.get()),
                    999999, 0, 0.0F));
            this.offers.add(new MerchantOffer(
                    new ItemCost(PaleLullabyItems.BLOOD_MARK.get(), BLOOD_MARK_COST),
                    new ItemStack(PaleLullabyItems.IRON_DEW_BADGE.get()),
                    999999, 0, 0.0F));
            this.offers.add(new MerchantOffer(
                    new ItemCost(PaleLullabyItems.BLOOD_MARK.get(), BLOOD_MARK_COST),
                    new ItemStack(PaleLullabyItems.INNOCENTS_TONGUE.get()),
                    999999, 0, 0.0F));
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.BAT_TAKEOFF;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    // ===== 声音 =====

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }
}
