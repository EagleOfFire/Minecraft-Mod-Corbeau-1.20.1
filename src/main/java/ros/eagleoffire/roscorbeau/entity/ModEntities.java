package ros.eagleoffire.roscorbeau.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ROSCorbeau.MODID);

    public static final RegistryObject<EntityType<CorbeauEntity>> CORBEAU = ENTITY_TYPES.register("corbeau",
            () -> EntityType.Builder.of(CorbeauEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.5F) // Width and height of the hitbox
                    .build(new ResourceLocation(ROSCorbeau.MODID, "corbeau").toString()));


    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
