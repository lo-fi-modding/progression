package lofimodding.progression.capabilities;

import lofimodding.progression.Stage;
import net.minecraft.entity.LivingEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Progress {
  public static Progress get(final LivingEntity entity) {
    return entity.getCapability(ProgressCapability.CAPABILITY).orElseThrow(RuntimeException::new);
  }

  private final Set<Stage> stages = new HashSet<>();
  private final Set<Stage> stagesImm = Collections.unmodifiableSet(this.stages);

  public void reset() {
    this.stages.clear();
  }

  public Set<Stage> getStages() {
    return this.stagesImm;
  }

  public void grantStage(final Stage... stages) {
    for(final Stage stage : stages) {
      this.stages.add(Objects.requireNonNull(stage, "Stage may not be null"));
    }
  }

  public void revokeStage(final Stage... stages) {
    for(final Stage stage : stages) {
      this.stages.remove(Objects.requireNonNull(stage, "Stage may not be null"));
    }
  }

  public boolean hasStage(final Stage... stages) {
    for(final Stage stage : stages) {
      if(!this.stages.contains(Objects.requireNonNull(stage, "Stage may not be null"))) {
        return false;
      }
    }

    return true;
  }
}
