package ros.eagleoffire.roscorbeau.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;
import ros.eagleoffire.roscorbeau.network.ModMessages;
import ros.eagleoffire.roscorbeau.network.packets.CorbeauSendParcheminC2SPacket;
import ros.eagleoffire.roscorbeau.screen.ParcheminScelleScreen;
import ros.eagleoffire.roscorbeau.screen.TargetPlayerNameScreen;

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

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof CorbeauEntity corbeau)) return super.interactLivingEntity(stack, player, target, hand);
        if (player.level().isClientSide) {
            ItemStack toStore = stack.copyWithCount(1);
            corbeau.setStoredParchemin(toStore);
            corbeau.setTransformed(!corbeau.isTransformed());
            stack.shrink(1);
            Minecraft.getInstance().setScreen(new TargetPlayerNameScreen(name -> {
                System.out.println("Sending packet with name: " + name);
                ModMessages.sendToServer(new CorbeauSendParcheminC2SPacket(corbeau.getId(), name));
            }));
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }
}
