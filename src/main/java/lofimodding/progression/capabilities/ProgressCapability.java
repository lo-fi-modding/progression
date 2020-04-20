package lofimodding.progression.capabilities;

import lofimodding.progression.ProgressionMod;
import lofimodding.progression.Stage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;

public class ProgressCapability {
  private ProgressCapability() { }

  public static final ResourceLocation ID = ProgressionMod.loc("knowledge");

  @CapabilityInject(Progress.class)
  public static Capability<Progress> CAPABILITY;

  public static void register() {
    CapabilityManager.INSTANCE.register(Progress.class, new Capability.IStorage<Progress>() {
      @Override
      public INBT writeNBT(final Capability<Progress> capability, final Progress instance, final Direction side) {
        final ListNBT stagesNbt = new ListNBT();
        for(final Stage stage : instance.getStages()) {
          stagesNbt.add(StringNBT.valueOf(stage.getRegistryName().toString()));
        }

        final CompoundNBT tag = new CompoundNBT();
        tag.put("stages", stagesNbt);
        return tag;
      }

      @Override
      public void readNBT(final Capability<Progress> capability, final Progress instance, final Direction side, final INBT base) {
        if(!(base instanceof CompoundNBT)) {
          return;
        }

        instance.reset();

        final CompoundNBT tag = (CompoundNBT)base;

        final ListNBT stagesNbt = tag.getList("stages", Constants.NBT.TAG_COMPOUND);
        for(final Stage stage : Stage.REGISTRY.get()) {
          if(stagesNbt.contains(stage.getRegistryName().toString())) {
            instance.grantStage(stage);
          }
        }
      }
    }, Progress::new);
  }
}
