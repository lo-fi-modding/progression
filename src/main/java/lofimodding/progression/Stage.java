package lofimodding.progression;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ProgressionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Stage extends ForgeRegistryEntry<Stage> {
  private static IForgeRegistry<Stage> registry;

  public static final Supplier<IForgeRegistry<Stage>> REGISTRY = new Supplier<IForgeRegistry<Stage>>() {
    @Override
    public IForgeRegistry<Stage> get() {
      return registry;
    }
  };

  @SubscribeEvent
  public static void createRegistry(final RegistryEvent.NewRegistry event) {
    ProgressionMod.LOGGER.info("Creating stage registry...");

    registry = new RegistryBuilder<Stage>()
      .setName(ProgressionMod.loc("research"))
      .setType(Stage.class)
      .create();
  }
}
