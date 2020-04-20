package lofimodding.progression.network;

import lofimodding.progression.Stage;
import lofimodding.progression.capabilities.CapabilityEvents;
import lofimodding.progression.capabilities.Progress;
import lofimodding.progression.capabilities.ProgressCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public class UpdatePlayerProgressPacket {
  public static void send(final ServerPlayerEntity player) {
    Packets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdatePlayerProgressPacket(player));
  }

  private static final Capability<Progress> CAP = ProgressCapability.CAPABILITY;

  private final int entityId;
  private final Progress progress;

  public UpdatePlayerProgressPacket(final PlayerEntity player) {
    this(player.getEntityId(), Progress.get(player));
  }

  public UpdatePlayerProgressPacket(final int entityId, final Progress progress) {
    this.entityId = entityId;
    this.progress = progress;
  }

  public static void encode(final UpdatePlayerProgressPacket packet, final PacketBuffer buffer) {
    buffer.writeVarInt(packet.entityId);

    buffer.writeVarInt(packet.progress.getStages().size());
    for(final Stage stage : packet.progress.getStages()) {
      buffer.writeString(stage.getRegistryName().toString(), 255);
    }
  }

  public static UpdatePlayerProgressPacket decode(final PacketBuffer buffer) {
    final IForgeRegistry<Stage> stages = Stage.REGISTRY.get();

    final Progress progress = new Progress();

    final int playerId = buffer.readVarInt();
    final int stageCount = buffer.readVarInt();

    for(int i = 0; i < stageCount; i++) {
      progress.grantStage(stages.getValue(new ResourceLocation(buffer.readString(255))));
    }

    return new UpdatePlayerProgressPacket(playerId, progress);
  }

  public static void handle(final UpdatePlayerProgressPacket packet, final Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      final Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityId);

      if(entity == null) {
        CapabilityEvents.deferUpdate(packet.entityId, packet.progress);
        return;
      }

      entity
        .getCapability(CAP, null)
        .ifPresent(progress -> CAP.readNBT(progress, null, CAP.writeNBT(packet.progress, null)));
    });
  }
}
