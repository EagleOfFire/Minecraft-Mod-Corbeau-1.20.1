package ros.eagleoffire.roscorbeau.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CorbeauRenderer extends GeoEntityRenderer<CorbeauEntity> {
    public CorbeauRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new CorbeauModel());
    }

    @Override
    public ResourceLocation getTextureLocation(CorbeauEntity animatable) {
        return new ResourceLocation(ROSCorbeau.MODID, "textures/entity/corbeau.png");
    }

    @Override
    public void render(CorbeauEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.4f,0.4f,0.4f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
