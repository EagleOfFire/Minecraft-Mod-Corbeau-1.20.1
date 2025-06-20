package ros.eagleoffire.roscorbeau.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;

import java.util.function.Supplier;

public class CorbeauSendParcheminC2SPacket {
    private final int entityId;
    private final String targetName;

    public CorbeauSendParcheminC2SPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.targetName = buf.readUtf();
    }

    public CorbeauSendParcheminC2SPacket(int entityId, String targetName) {
        this.entityId = entityId;
        this.targetName = targetName;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.targetName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        System.out.println("packet send");
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = supplier.get().getSender();
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(entityId);
            if (entity instanceof CorbeauEntity corbeau) {
                corbeau.setTargetPlayerName(targetName);
                corbeau.setTransformed(true);
                corbeau.triggerFlyAway();
            }
        });
    }
}
