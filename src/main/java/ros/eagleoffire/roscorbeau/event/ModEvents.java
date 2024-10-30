package ros.eagleoffire.roscorbeau.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.ModEntities;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;

@Mod.EventBusSubscriber(modid = ROSCorbeau.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CORBEAU.get(), CorbeauEntity.setAttribute());
    }
}
