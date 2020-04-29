package lofimodding.progression.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lofimodding.progression.ProgressionMod;
import lofimodding.progression.Stage;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class StagedRecipeBuilder {
  private StagedRecipeBuilder() { }

  public static Shaped shaped(final IItemProvider result) {
    return shaped(result, 1);
  }

  public static Shaped shaped(final IItemProvider result, final int amount) {
    return new Shaped(result, amount);
  }

  public static Shapeless shapelessRecipe(final IItemProvider item) {
    return new Shapeless(item, 1);
  }

  public static Shapeless shapelessRecipe(final IItemProvider item, final int amount) {
    return new Shapeless(item, amount);
  }

  public static class Shaped {
    private final Item result;
    private final int count;
    private final NonNullList<Stage> stages = NonNullList.create();
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
    private boolean damageTools = true;
    private String group;

    public Shaped(final IItemProvider resultIn, final int countIn) {
      this.result = resultIn.asItem();
      this.count = countIn;
    }

    public Shaped stage(final Stage stage) {
      this.stages.add(stage);
      return this;
    }

    /**
     * Adds a key to the recipe pattern.
     */
    public Shaped key(final Character symbol, final Tag<Item> tagIn) {
      return this.key(symbol, Ingredient.fromTag(tagIn));
    }

    /**
     * Adds a key to the recipe pattern.
     */
    public Shaped key(final Character symbol, final IItemProvider itemIn) {
      return this.key(symbol, Ingredient.fromItems(itemIn));
    }

    /**
     * Adds a key to the recipe pattern.
     */
    public Shaped key(final Character symbol, final Ingredient ingredientIn) {
      if(this.key.containsKey(symbol)) {
        throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
      }

      if(symbol == ' ') {
        throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      }

      this.key.put(symbol, ingredientIn);
      return this;
    }

    /**
     * Adds a new entry to the patterns for this recipe.
     */
    public Shaped patternLine(final String patternIn) {
      if(!this.pattern.isEmpty() && patternIn.length() != this.pattern.get(0).length()) {
        throw new IllegalArgumentException("Pattern must be the same width on every line!");
      }

      this.pattern.add(patternIn);
      return this;
    }

    /**
     * Adds a criterion needed to unlock the recipe.
     */
    public Shaped addCriterion(final String name, final ICriterionInstance criterionIn) {
      this.advancementBuilder.withCriterion(name, criterionIn);
      return this;
    }

    public Shaped damageTools(final boolean damageTools) {
      this.damageTools = damageTools;
      return this;
    }

    public Shaped setGroup(final String groupIn) {
      this.group = groupIn;
      return this;
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}.
     */
    public void build(final Consumer<IFinishedRecipe> consumerIn) {
      this.build(consumerIn, ForgeRegistries.ITEMS.getKey(this.result));
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}. Use {@link #build(Consumer)} if save is the same as the ID for
     * the result.
     */
    public void build(final Consumer<IFinishedRecipe> consumerIn, final String save) {
      final ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);

      if(new ResourceLocation(save).equals(resourcelocation)) {
        throw new IllegalStateException("Shaped Staged Recipe " + save + " should remove its 'save' argument");
      }

      this.build(consumerIn, new ResourceLocation(save));
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}.
     */
    public void build(final Consumer<IFinishedRecipe> consumer, final ResourceLocation id) {
      this.validate(id);
      this.advancementBuilder.withParentId(new ResourceLocation("recipes/root")).withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(id)).withRewards(AdvancementRewards.Builder.recipe(id)).withRequirementsStrategy(IRequirementsStrategy.OR);
      consumer.accept(new Result(id, this.stages, this.result, this.count, this.damageTools, this.group == null ? "" : this.group, this.pattern, this.key, this.advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + this.result.getGroup().getPath() + '/' + id.getPath())));
    }

    /**
     * Makes sure that this recipe is valid and obtainable.
     */
    private void validate(final ResourceLocation id) {
      if(this.pattern.isEmpty()) {
        throw new IllegalStateException("No pattern is defined for shaped recipe " + id + '!');
      }

      final Set<Character> set = Sets.newHashSet(this.key.keySet());
      set.remove(' ');

      for(final String s : this.pattern) {
        for(int i = 0; i < s.length(); ++i) {
          final char c0 = s.charAt(i);

          if(!this.key.containsKey(c0) && c0 != ' ') {
            throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + '\'');
          }

          set.remove(c0);
        }
      }

      if(!set.isEmpty()) {
        throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
      }

      if(this.pattern.size() == 1 && this.pattern.get(0).length() == 1) {
        throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item - should it be a shapeless recipe instead?");
      }

      if(this.advancementBuilder.getCriteria().isEmpty()) {
        throw new IllegalStateException("No way of obtaining recipe " + id);
      }
    }

    public static class Result implements IFinishedRecipe {
      private final ResourceLocation id;
      private final NonNullList<Stage> stages;
      private final Item result;
      private final int count;
      private final boolean damageTools;
      private final String group;
      private final List<String> pattern;
      private final Map<Character, Ingredient> key;
      private final Advancement.Builder advancementBuilder;
      private final ResourceLocation advancementId;

      public Result(final ResourceLocation id, final NonNullList<Stage> stages, final Item result, final int count, final boolean damageTools, final String group, final List<String> pattern, final Map<Character, Ingredient> key, final Advancement.Builder advancementBuilder, final ResourceLocation advancementId) {
        this.id = id;
        this.stages = stages;
        this.result = result;
        this.count = count;
        this.damageTools = damageTools;
        this.group = group;
        this.pattern = pattern;
        this.key = key;
        this.advancementBuilder = advancementBuilder;
        this.advancementId = advancementId;
      }

      @Override
      public void serialize(final JsonObject json) {
        if(!this.group.isEmpty()) {
          json.addProperty("group", this.group);
        }

        final JsonArray stages = new JsonArray();
        for(final Stage stage : this.stages) {
          stages.add(stage.getRegistryName().toString());
        }
        json.add("stages", stages);
        json.addProperty("damage_tools", this.damageTools);

        final JsonArray pattern = new JsonArray();
        for(final String s : this.pattern) {
          pattern.add(s);
        }

        json.add("pattern", pattern);
        final JsonObject jsonobject = new JsonObject();

        for(final Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
          jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().serialize());
        }

        json.add("key", jsonobject);
        final JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("item", this.result.getRegistryName().toString());
        if(this.count > 1) {
          jsonobject1.addProperty("count", this.count);
        }

        json.add("result", jsonobject1);
      }

      @Override
      public IRecipeSerializer<?> getSerializer() {
        return ProgressionMod.SHAPED_STAGED_RECIPE_SERIALIZER.get();
      }

      /**
       * Gets the ID for the recipe.
       */
      @Override
      public ResourceLocation getID() {
        return this.id;
      }

      /**
       * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
       */
      @Override
      @Nullable
      public JsonObject getAdvancementJson() {
        return this.advancementBuilder.serialize();
      }

      /**
       * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson}
       * is non-null.
       */
      @Override
      @Nullable
      public ResourceLocation getAdvancementID() {
        return this.advancementId;
      }
    }
  }

  public static class Shapeless {
    private final Item result;
    private final int count;
    private final NonNullList<Stage> stages = NonNullList.create();
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
    private boolean damageTools = true;
    private String group;

    public Shapeless(final IItemProvider item, final int amount) {
      this.result = item.asItem();
      this.count = amount;
    }

    public Shapeless stage(final Stage stage) {
      this.stages.add(stage);
      return this;
    }

    public Shapeless addIngredient(final Tag<Item> tag) {
      return this.addIngredient(Ingredient.fromTag(tag));
    }

    public Shapeless addIngredient(final IItemProvider item) {
      return this.addIngredient(item, 1);
    }

    public Shapeless addIngredient(final IItemProvider item, final int amount) {
      for(int i = 0; i < amount; ++i) {
        this.addIngredient(Ingredient.fromItems(item));
      }

      return this;
    }

    public Shapeless addIngredient(final Ingredient ingredient) {
      return this.addIngredient(ingredient, 1);
    }

    public Shapeless addIngredient(final Ingredient ingredient, final int amount) {
      for(int i = 0; i < amount; ++i) {
        this.ingredients.add(ingredient);
      }

      return this;
    }

    public Shapeless addCriterion(final String key, final ICriterionInstance criterion) {
      this.advancementBuilder.withCriterion(key, criterion);
      return this;
    }

    public Shapeless damageTools(final boolean damageTools) {
      this.damageTools = damageTools;
      return this;
    }

    public Shapeless setGroup(final String group) {
      this.group = group;
      return this;
    }

    public void build(final Consumer<IFinishedRecipe> finished) {
      this.build(finished, this.result.getRegistryName());
    }

    public void build(final Consumer<IFinishedRecipe> finished, final String save) {
      final ResourceLocation name = this.result.getRegistryName();
      if(new ResourceLocation(save).equals(name)) {
        throw new IllegalStateException("Shapeless Staged Recipe " + save + " should remove its 'save' argument");
      }

      this.build(finished, new ResourceLocation(save));
    }

    public void build(final Consumer<IFinishedRecipe> finished, final ResourceLocation save) {
      this.validate(save);
      this.advancementBuilder.withParentId(new ResourceLocation("recipes/root")).withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(save)).withRewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(save)).withRequirementsStrategy(IRequirementsStrategy.OR);
      finished.accept(new Result(save, this.stages, this.result, this.count, this.damageTools, this.group == null ? "" : this.group, this.ingredients, this.advancementBuilder, new ResourceLocation(save.getNamespace(), "recipes/" + this.result.getGroup().getPath() + "/" + save.getPath())));
    }

    private void validate(final ResourceLocation name) {
      if(this.advancementBuilder.getCriteria().isEmpty()) {
        throw new IllegalStateException("No way of obtaining recipe " + name);
      }
    }

    public static class Result implements IFinishedRecipe {
      private final ResourceLocation id;
      private final NonNullList<Stage> stages;
      private final Item result;
      private final int count;
      private final boolean damageTools;
      private final String group;
      private final List<Ingredient> ingredients;
      private final Advancement.Builder advancementBuilder;
      private final ResourceLocation advancementId;

      public Result(final ResourceLocation id, final NonNullList<Stage> stages, final Item result, final int count, final boolean damageTools, final String group, final List<Ingredient> ingredients, final Advancement.Builder advancementBuilder, final ResourceLocation advancementId) {
        this.id = id;
        this.stages = stages;
        this.result = result;
        this.count = count;
        this.damageTools = damageTools;
        this.group = group;
        this.ingredients = ingredients;
        this.advancementBuilder = advancementBuilder;
        this.advancementId = advancementId;
      }

      @Override
      public void serialize(final JsonObject json) {
        if(!this.group.isEmpty()) {
          json.addProperty("group", this.group);
        }

        final JsonArray stages = new JsonArray();
        for(final Stage stage : this.stages) {
          stages.add(stage.getRegistryName().toString());
        }
        json.add("stages", stages);
        json.addProperty("damage_tools", this.damageTools);

        final JsonArray array = new JsonArray();
        for(final Ingredient ingredient : this.ingredients) {
          array.add(ingredient.serialize());
        }

        json.add("ingredients", array);

        final JsonObject result = new JsonObject();
        result.addProperty("item", this.result.getRegistryName().toString());

        if(this.count > 1) {
          result.addProperty("count", this.count);
        }

        json.add("result", result);
      }

      @Override
      public IRecipeSerializer<?> getSerializer() {
        return ProgressionMod.SHAPELESS_STAGED_RECIPE_SERIALIZER.get();
      }

      @Override
      public ResourceLocation getID() {
        return this.id;
      }

      @Override
      @Nullable
      public JsonObject getAdvancementJson() {
        return this.advancementBuilder.serialize();
      }

      @Override
      @Nullable
      public ResourceLocation getAdvancementID() {
        return this.advancementId;
      }
    }
  }
}
