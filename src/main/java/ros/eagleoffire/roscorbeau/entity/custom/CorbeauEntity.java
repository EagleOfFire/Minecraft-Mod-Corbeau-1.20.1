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
import software.bernie.geckolib.util.GeckoLibUtil;

public class CorbeauEntity extends Animal implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private boolean transformed = false;
    private static final EntityDataAccessor<Boolean> TRANSFORMED =
            SynchedEntityData.defineId(CorbeauEntity.class, EntityDataSerializers.BOOLEAN);
    private ItemStack storedParchemin = ItemStack.EMPTY;
    private String targetPlayerName = "";

    public CorbeauEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttribute() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.4f)
                .add(Attributes.FLYING_SPEED, 0.6f).build();
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if (tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.corbeau.marcher", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.corbeau.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    public boolean isTransformed() {
        return this.entityData.get(TRANSFORMED);
    }

    public void setTransformed(boolean transformed) {
        this.entityData.set(TRANSFORMED, transformed);
    }

    public ItemStack getStoredParchemin() {
        return storedParchemin;
    }

    public void setStoredParchemin(ItemStack stack) {
        this.storedParchemin = stack;
    }

    public void triggerFlyAway() {
        System.out.println(this.isNoGravity());
        if (!level().isClientSide) {
            this.setNoGravity(true);
            double groundY = this.blockPosition().getY();

            // Scan downward to find the first solid block under the entity
            for (int y = this.blockPosition().getY(); y > level().getMinBuildHeight(); y--) {
                if (!level().getBlockState(this.blockPosition().offset(0, y - this.blockPosition().getY(), 0)).isAir()) {
                    groundY = y;
                    break;
                }
            }

            // Set the position to 3 blocks above ground
            this.setPos(this.getX(), groundY + 3, this.getZ());

            // Optionally stop any vertical movement to prevent drifting
            this.setDeltaMovement(this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
        }
    }

    public void setTargetPlayerName(String name) {
        this.targetPlayerName = name;
    }

    public String getTargetPlayerName() {
        return this.targetPlayerName;
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(1, new FloatGoal(this));
        //this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return ModEntities.CORBEAU.get().create(serverLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllerRegistrar.add(
                new AnimationController<>(this, "fly_controller", 10, state -> {
                    if (this.isTransformed()) {
                        return state.setAndContinue(RawAnimation.begin().then("animation.corbeau.voler", Animation.LoopType.LOOP));
                    }
                    return PlayState.STOP;
                })
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Transformed", isTransformed());

        if (!storedParchemin.isEmpty()) {
            tag.put("StoredParchemin", storedParchemin.save(new CompoundTag()));
        }

        tag.putString("TargetPlayer", targetPlayerName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.transformed = tag.getBoolean("Transformed");

        if (tag.contains("StoredParchemin")) {
            storedParchemin = ItemStack.of(tag.getCompound("StoredParchemin"));
        }

        targetPlayerName = tag.getString("TargetPlayer");
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
        if (!level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.isEmpty() && player.isShiftKeyDown() && isTransformed()) {
                if (!storedParchemin.isEmpty()) {
                    boolean success = player.getInventory().add(storedParchemin.copy());
                    if (!success) {
                        player.drop(storedParchemin.copy(), false);
                    }
                    storedParchemin = ItemStack.EMPTY;
                }
                setTransformed(false);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }


}
