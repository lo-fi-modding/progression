package lofimodding.progression.integrations.jei;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import lofimodding.progression.Stage;
import lofimodding.progression.recipes.IStagedRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class ShapelessStagedCraftingExtension<T extends IRecipe<CraftingInventory> & IStagedRecipe> implements ICraftingCategoryExtension {
  protected final T recipe;

  public ShapelessStagedCraftingExtension(final T recipe) {
    this.recipe = recipe;
  }

  @Override
  public ResourceLocation getRegistryName() {
    return this.recipe.getId();
  }

  @Override
  public void drawInfo(final int recipeWidth, final int recipeHeight, final double mouseX, final double mouseY) {
    RenderSystem.pushMatrix();
    RenderSystem.translatef(103.0f, 42.0f, 0.0f);
    RenderSystem.scalef(0.75f, 0.75f, 0.0f);

    for(final Stage stage : this.recipe.getStages()) {
      Minecraft.getInstance().textureManager.bindTexture(stage.getIcon());
      AbstractGui.blit(0, 0, 0, 0.0f, 0.0f, 16, 16, 16, 16);
      RenderSystem.translatef(-16.0f, 0.0f, 0.0f);
    }

    RenderSystem.popMatrix();
  }

  @Override
  public List<String> getTooltipStrings(final double mouseX, final double mouseY) {
    if(mouseY >= 42 && mouseY <= 54) {
      for(int i = 0; i < this.recipe.getStages().size(); i++) {
        if(mouseX >= 103 - i * 16 && mouseX <= 115 - i * 16) {
          final ITextComponent stage = this.recipe.getStages().get(i).getName();
          final ITextComponent requirement = new TranslationTextComponent("jei.stage.requirement", stage);
          return Lists.newArrayList(requirement.getFormattedText());
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  public void setIngredients(final IIngredients ingredients) {
    ingredients.setInputIngredients(this.recipe.getIngredients());
    ingredients.setOutput(VanillaTypes.ITEM, this.recipe.getRecipeOutput());
  }
}
