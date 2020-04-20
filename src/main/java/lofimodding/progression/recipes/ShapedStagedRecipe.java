package lofimodding.progression.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lofimodding.progression.ProgressionMod;
import lofimodding.progression.Stage;
import lofimodding.progression.StageUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Random;

public class ShapedStagedRecipe implements IStagedRecipe, ICraftingRecipe, IShapedRecipe<CraftingInventory> {
  private static final Random RAND = new Random();

  private final ShapedRecipe recipe;
  private final NonNullList<Stage> stages;

  public ShapedStagedRecipe(final ShapedRecipe recipe, final NonNullList<Stage> stages) {
    this.recipe = recipe;
    this.stages = stages;
  }

  @Override
  public NonNullList<Stage> getStages() {
    return this.stages;
  }

  @Override
  public int getRecipeWidth() {
    return this.recipe.getRecipeWidth();
  }

  @Override
  public int getRecipeHeight() {
    return this.recipe.getRecipeHeight();
  }

  @Override
  public boolean matches(final CraftingInventory inv, final World world) {
    return StageUtils.hasStage(inv, this.stages.toArray(new Stage[0])) && this.recipe.matches(inv, world);
  }

  @Override
  public ItemStack getCraftingResult(final CraftingInventory inv) {
    return this.recipe.getCraftingResult(inv);
  }

  @Override
  public boolean canFit(final int width, final int height) {
    return this.recipe.canFit(width, height);
  }

  @Override
  public ItemStack getRecipeOutput() {
    return this.recipe.getRecipeOutput();
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(final CraftingInventory inv) {
    final NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

    for(int i = 0; i < list.size(); ++i) {
      final ItemStack stack = inv.getStackInSlot(i);

      if(stack.getItem() instanceof ToolItem) {
        stack.attemptDamageItem(1, RAND, null);

        if(stack.isDamageable() && stack.getDamage() > stack.getMaxDamage()) {
          list.set(i, ItemStack.EMPTY);
        } else {
          list.set(i, stack.copy());
        }
      } else {
        list.set(i, ForgeHooks.getContainerItem(stack));
      }
    }

    return list;
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return this.recipe.getIngredients();
  }

  @Override
  public boolean isDynamic() {
    return this.recipe.isDynamic();
  }

  @Override
  public String getGroup() {
    return this.recipe.getGroup();
  }

  @Override
  public ItemStack getIcon() {
    return this.recipe.getIcon();
  }

  @Override
  public ResourceLocation getId() {
    return this.recipe.getId();
  }

  @Override
  public IRecipeType<?> getType() {
    return this.recipe.getType();
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return ProgressionMod.SHAPED_STAGED_RECIPE_SERIALIZER.get();
  }

  public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ShapedStagedRecipe> {
    @Override
    public ShapedStagedRecipe read(final ResourceLocation id, final JsonObject json) {
      final ShapedRecipe recipe = IRecipeSerializer.CRAFTING_SHAPED.read(id, json);

      final NonNullList<Stage> stages = NonNullList.create();
      for(final JsonElement element : JSONUtils.getJsonArray(json, "stages", new JsonArray())) {
        stages.add(Stage.REGISTRY.get().getValue(new ResourceLocation(element.getAsString())));
      }

      return new ShapedStagedRecipe(recipe, stages);
    }

    @Override
    public ShapedStagedRecipe read(final ResourceLocation id, final PacketBuffer buffer) {
      final ShapedRecipe recipe = IRecipeSerializer.CRAFTING_SHAPED.read(id, buffer);
      final NonNullList<Stage> stages = NonNullList.create();

      final int stageCount = buffer.readVarInt();
      for(int i = 0; i < stageCount; i++) {
        stages.add(buffer.readRegistryIdSafe(Stage.class));
      }

      return new ShapedStagedRecipe(recipe, stages);
    }

    @Override
    public void write(final PacketBuffer buffer, final ShapedStagedRecipe recipe) {
      IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe.recipe);
      buffer.writeVarInt(recipe.stages.size());

      for(final Stage stage : recipe.stages) {
        buffer.writeRegistryId(stage);
      }
    }
  }
}
