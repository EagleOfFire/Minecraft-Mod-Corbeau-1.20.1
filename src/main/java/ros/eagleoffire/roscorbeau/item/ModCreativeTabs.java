package ros.eagleoffire.roscorbeau.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.item.ModItems;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ROSCorbeau.MODID);

    public static final RegistryObject<CreativeModeTab> ROS_CUISINE_TAB = CREATIVE_MODE_TABS.register("ros_corbeau_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CORBEAU_SPAWN_EGG.get()))
                    .title(Component.translatable("creativetab.ros_corbeau_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.CORBEAU_SPAWN_EGG.get());
                        pOutput.accept(ModItems.PARCHEMIN.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
