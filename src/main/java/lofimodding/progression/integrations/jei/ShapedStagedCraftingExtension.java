package lofimodding.progression.integrations.jei;

import lofimodding.progression.recipes.ShapedStagedRecipe;
import net.minecraftforge.common.util.Size2i;

public class ShapedStagedCraftingExtension extends ShapelessStagedCraftingExtension<ShapedStagedRecipe> {
  public ShapedStagedCraftingExtension(final ShapedStagedRecipe recipe) {
    super(recipe);
  }

  @Override
  public Size2i getSize() {
    return new Size2i(this.recipe.getRecipeWidth(), this.recipe.getRecipeHeight());
  }
}
