package ros.eagleoffire.roscorbeau.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.ModEntities;
import ros.eagleoffire.roscorbeau.item.custom.ModSpawnEgg;
import ros.eagleoffire.roscorbeau.item.custom.ParcheminItem;
import ros.eagleoffire.roscorbeau.item.custom.ParcheminScelleItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ROSCorbeau.MODID);

    public static final RegistryObject<Item> CORBEAU_SPAWN_EGG = ITEMS.register("corbeau_spawn_egg",
            () -> new ModSpawnEgg(() -> ModEntities.CORBEAU.get(), new Item.Properties()));

    public static final RegistryObject<Item> PARCHEMIN = ITEMS.register("parchemin",
        () -> new ParcheminItem(new Item.Properties()));

    public static final RegistryObject<Item> PARCHEMIN_SCELLE = ITEMS.register("parchemin_scelle",
        () -> new ParcheminScelleItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) { ITEMS.register(eventBus);}
}
