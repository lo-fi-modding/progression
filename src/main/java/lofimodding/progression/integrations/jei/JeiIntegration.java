package lofimodding.progression.integrations.jei;

import lofimodding.progression.ProgressionMod;
import lofimodding.progression.recipes.ShapedStagedRecipe;
import lofimodding.progression.recipes.ShapelessStagedRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JeiIntegration implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return ProgressionMod.loc("progression");
  }

  @Override
  public void registerVanillaCategoryExtensions(final IVanillaCategoryExtensionRegistration registration) {
    registration.getCraftingCategory().addCategoryExtension(ShapedStagedRecipe.class, ShapedStagedCraftingExtension::new);
    registration.getCraftingCategory().addCategoryExtension(ShapelessStagedRecipe.class, ShapelessStagedCraftingExtension::new);
  }
}
