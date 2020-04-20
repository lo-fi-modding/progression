package lofimodding.progression;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lofimodding.progression.capabilities.ProgressCapability;
import lofimodding.progression.commands.StageCommand;
import lofimodding.progression.network.Packets;
import lofimodding.progression.recipes.ShapedStagedRecipe;
import lofimodding.progression.recipes.ShapelessStagedRecipe;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
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
    MinecraftForge.EVENT_BUS.addListener(this::serverStarting);

    RECIPE_SERIALIZERS.register(bus);
  }

  private void setup(final FMLCommonSetupEvent event) {
    Packets.register();
    ProgressCapability.register();
  }

  private void serverStarting(final FMLServerStartingEvent event) {
    final LiteralArgumentBuilder<CommandSource> root = Commands.literal(MOD_ID)
      .then(StageCommand.build());

    event.getCommandDispatcher().register(root);
  }

  public static ResourceLocation loc(final String path) {
    return new ResourceLocation(MOD_ID, path);
  }
}
