package lofimodding.progression.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProgressProvider implements ICapabilitySerializable<INBT> {
  private final Capability<Progress> cap = ProgressCapability.CAPABILITY;
  private final Progress instance = this.cap.getDefaultInstance();
  private final LazyOptional<Progress> lazy = LazyOptional.of(() -> this.instance);

  @Override
  @Nullable
  public INBT serializeNBT() {
    return this.cap.writeNBT(this.instance, null);
  }

  @Override
  public void deserializeNBT(final INBT nbt) {
    this.cap.readNBT(this.instance, null, nbt);
  }

  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, @Nullable final Direction facing) {
    return this.cap.orEmpty(capability, this.lazy);
  }
}
