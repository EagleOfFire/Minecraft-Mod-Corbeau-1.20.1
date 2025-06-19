package ros.eagleoffire.roscorbeau.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import ros.eagleoffire.roscorbeau.entity.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

public class CorbeauEntity extends Animal implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private boolean transformed = false;
    private static final EntityDataAccessor<Boolean> TRANSFORMED =
            SynchedEntityData.defineId(CorbeauEntity.class, EntityDataSerializers.BOOLEAN);

    public CorbeauEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttribute() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.4f).build();
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if(tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.corbeau.marcher", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.corbeau.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    public boolean isTransformed() {
        return this.entityData.get(TRANSFORMED);
    }

    public void setTransformed(boolean transformed){
        this.entityData.set(TRANSFORMED, transformed);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D,false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class,true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class,false));
        this.targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Creeper.class, true));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return ModEntities.CORBEAU.get().create(serverLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.transformed = tag.getBoolean("Transformed");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Transformed", isTransformed());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRANSFORMED, false);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide  && hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.isEmpty() && player.isShiftKeyDown()) {
                if (isTransformed()) {
                    setTransformed(false);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.mobInteract(player, hand);
    }
}
