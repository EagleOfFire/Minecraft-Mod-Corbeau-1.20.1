package ros.eagleoffire.roscorbeau.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.entity.custom.CorbeauEntity;
import ros.eagleoffire.roscorbeau.network.packets.CorbeauSendParcheminC2SPacket;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id(){
        return packetId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ROSCorbeau.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(CorbeauSendParcheminC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CorbeauSendParcheminC2SPacket::new)
                .encoder(CorbeauSendParcheminC2SPacket::toBytes)
                .consumerMainThread(CorbeauSendParcheminC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

}
