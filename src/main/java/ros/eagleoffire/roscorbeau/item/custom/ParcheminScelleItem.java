package ros.eagleoffire.roscorbeau.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;
import ros.eagleoffire.roscorbeau.screen.ParcheminScelleScreen;
import ros.eagleoffire.roscorbeau.screen.ParcheminScreen;

public class ParcheminScelleItem extends WrittenBookItem {
    public ParcheminScelleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            ParcheminScelleScreen.BookAccess access = ParcheminScelleScreen.BookAccess.fromItem(stack);
            Minecraft.getInstance().setScreen(new ParcheminScelleScreen(access));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

}
