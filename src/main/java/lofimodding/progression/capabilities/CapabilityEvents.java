package lofimodding.progression.capabilities;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lofimodding.progression.ProgressionMod;
import lofimodding.progression.network.UpdatePlayerProgressPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressionMod.MOD_ID)
public final class CapabilityEvents {
  private static final Int2ObjectMap<Progress> deferredUpdates = new Int2ObjectAVLTreeMap<>();

  private CapabilityEvents() { }

  @SubscribeEvent
  public static void attachOnSpawn(final AttachCapabilitiesEvent<Entity> event) {
    if(event.getObject() instanceof PlayerEntity) {
      event.addCapability(ProgressCapability.ID, new ProgressProvider());
    }
  }

  @SubscribeEvent
  public static void onSpawn(final EntityJoinWorldEvent event) {
    if(event.getEntity() instanceof PlayerEntity) {
      if(event.getWorld().isRemote) {
        final Progress newProgress = deferredUpdates.remove(event.getEntity().getEntityId());

        if(newProgress != null) {
          event.getEntity()
            .getCapability(ProgressCapability.CAPABILITY)
            .ifPresent(oldProgress -> copy(oldProgress, newProgress));
        }

        return;
      }

      if(event.getEntity() instanceof ServerPlayerEntity) {
        UpdatePlayerProgressPacket.send((ServerPlayerEntity)event.getEntity());
      }
    }
  }

  @SubscribeEvent
  public static void playerClone(final PlayerEvent.Clone event) {
    if(event.isWasDeath()) {
      event.getPlayer().getCapability(ProgressCapability.CAPABILITY).ifPresent(newProgress -> event.getOriginal().getCapability(ProgressCapability.CAPABILITY).ifPresent(oldProgress -> copy(oldProgress, newProgress)));
    }
  }

  private static void copy(final Progress oldProgress, final Progress newProgress) {
    ProgressCapability.CAPABILITY.readNBT(newProgress, null, ProgressCapability.CAPABILITY.writeNBT(oldProgress, null));
  }

  public static void deferUpdate(final int id, final Progress progress) {
    deferredUpdates.put(id, progress);
  }
}
