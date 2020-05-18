package lofimodding.progression;

import lofimodding.progression.capabilities.Progress;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.WorkbenchContainer;

import javax.annotation.Nullable;

public final class StageUtils {
  private StageUtils() { }

  public static void grantStage(final LivingEntity player, final Stage... stages) {
    Progress.get(player).grantStage(stages);

    if(player instanceof ServerPlayerEntity) {
      ProgressionMod.STAGE_UNLOCKED.trigger((ServerPlayerEntity)player);
    }
  }

  public static boolean hasStage(final LivingEntity player, final Stage... stages) {
    return Progress.get(player).hasStage(stages);
  }

  public static boolean hasStage(final CraftingInventory inv, final Stage... stages) {
    final PlayerEntity player = findPlayerFromInv(inv);
    return player != null && hasStage(player, stages);
  }

  @Nullable
  public static PlayerEntity findPlayerFromInv(final CraftingInventory inv) {
    final Container container = inv.eventHandler;

    if(container instanceof PlayerContainer) {
      return ((PlayerContainer)container).player;
    }

    if(container instanceof WorkbenchContainer) {
      return ((WorkbenchContainer)container).player;
    }

    // Don't know how to find player
    return null;
  }
}
