package lofimodding.progression.network;

import lofimodding.progression.ProgressionMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class Packets {
  private Packets() { }

  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
    .named(new ResourceLocation(ProgressionMod.MOD_ID, "main_channel"))
    .clientAcceptedVersions(PROTOCOL_VERSION::equals)
    .serverAcceptedVersions(PROTOCOL_VERSION::equals)
    .networkProtocolVersion(() -> PROTOCOL_VERSION)
    .simpleChannel();

  private static int id;

  public static void register() {
    CHANNEL.messageBuilder(UpdatePlayerProgressPacket.class, id++).encoder(UpdatePlayerProgressPacket::encode).decoder(UpdatePlayerProgressPacket::decode).consumer(UpdatePlayerProgressPacket::handle).add();
  }
}
