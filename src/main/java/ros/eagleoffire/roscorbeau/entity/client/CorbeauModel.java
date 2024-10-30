package ros.eagleoffire.roscorbeau.entity.client;

import net.minecraft.resources.ResourceLocation;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class CorbeauModel extends GeoModel<CorbeauEntity> {
    @Override
    public ResourceLocation getModelResource(CorbeauEntity corbeauEntity) {
        return new ResourceLocation(ROSCorbeau.MODID, "geo/corbeau.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CorbeauEntity corbeauEntity) {
        return new ResourceLocation(ROSCorbeau.MODID, "textures/entity/corbeau.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CorbeauEntity corbeauEntity) {
        return new ResourceLocation(ROSCorbeau.MODID, "animations/corbeau.animation.json");
    }

    @Override
    public void setCustomAnimations(CorbeauEntity animatable, long instanceId, AnimationState<CorbeauEntity> animationState) {
    }
}
