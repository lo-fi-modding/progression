package lofimodding.progression.recipes;

import lofimodding.progression.Stage;
import net.minecraft.util.NonNullList;

public interface IStagedRecipe {
  NonNullList<Stage> getStages();
}
