package lofimodding.progression;

import lofimodding.progression.capabilities.ProgressCapability;
import lofimodding.progression.network.Packets;
import lofimodding.progression.recipes.ShapedStagedRecipe;
import lofimodding.progression.recipes.ShapelessStagedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProgressionMod.MOD_ID)
public class ProgressionMod {
  public static final String MOD_ID = "progression";
  public static final Logger LOGGER = LogManager.getLogger();

  private static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

  public static final RegistryObject<IRecipeSerializer<ShapedStagedRecipe>> SHAPED_STAGED_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("shaped_staged", ShapedStagedRecipe.Serializer::new);
  public static final RegistryObject<IRecipeSerializer<ShapelessStagedRecipe>> SHAPELESS_STAGED_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("shapeless_staged", ShapelessStagedRecipe.Serializer::new);

  public ProgressionMod() {
    final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

    bus.addListener(this::setup);

    RECIPE_SERIALIZERS.register(bus);
  }

  private void setup(final FMLCommonSetupEvent event) {
    Packets.register();
    ProgressCapability.register();
  }

  public static ResourceLocation loc(final String path) {
    return new ResourceLocation(MOD_ID, path);
  }
}
