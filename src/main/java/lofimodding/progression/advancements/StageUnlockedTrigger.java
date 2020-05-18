package lofimodding.progression.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lofimodding.progression.ProgressionMod;
import lofimodding.progression.Stage;
import lofimodding.progression.capabilities.Progress;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class StageUnlockedTrigger extends AbstractCriterionTrigger<StageUnlockedTrigger.Instance> {
  private static final ResourceLocation ID = ProgressionMod.loc("stage_unlocked");

  @Override
  public ResourceLocation getId() {
    return ID;
  }

  @Override
  public Instance deserializeInstance(final JsonObject json, final JsonDeserializationContext context) {
    final String id = JSONUtils.getString(json, "id");
    return new Instance(ID, new ResourceLocation(id));
  }

  public void trigger(final ServerPlayerEntity player) {
    this.func_227070_a_(player.getAdvancements(), instance -> instance.test(player));
  }

  public static class Instance extends CriterionInstance {
    public final ResourceLocation stage;

    public Instance(final ResourceLocation criterion, final ResourceLocation stage) {
      super(criterion);
      this.stage = stage;
    }

    public static Instance forStage(final Stage stage) {
      return new Instance(ID, stage.getRegistryName());
    }

    public boolean test(final ServerPlayerEntity player) {
      return Progress.get(player).hasStage(Stage.REGISTRY.get().getValue(this.stage));
    }

    @Override
    public JsonElement serialize() {
      final JsonObject json = new JsonObject();
      json.addProperty("id", this.stage.toString());
      return json;
    }
  }
}
