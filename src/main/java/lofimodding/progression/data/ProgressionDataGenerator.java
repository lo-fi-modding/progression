package lofimodding.progression.data;

import lofimodding.progression.ProgressionMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ProgressionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ProgressionDataGenerator {
  private ProgressionDataGenerator() { }

  @SubscribeEvent
  public static void gatherData(final GatherDataEvent event) {
    final DataGenerator gen = event.getGenerator();

    if(event.includeClient()) {
      gen.addProvider(new EnglishLang(gen));
    }
  }

  public static class EnglishLang extends LanguageProvider {
    public EnglishLang(final DataGenerator gen) {
      super(gen, ProgressionMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
      this.add("commands.stage.get.no_stages", "%s has no stages");
      this.add("commands.stage.get.stages_for", "Stages for %s:");
      this.add("commands.stage.get.stages_list", "- %s");
      this.add("commands.stage.grant.invalid_stage", "%s is not a valid stage");
      this.add("commands.stage.grant.granted", "You have been granted stage %s");
      this.add("commands.stage.grant.granted_to", "%s has been granted stage %s");
      this.add("commands.stage.revoke.invalid_stage", "%s is not a valid stage");
      this.add("commands.stage.revoke.revoked", "You have had stage %s revoked");
      this.add("commands.stage.revoke.revoked_to", "%s has had stage %s revoked");

      this.add("jei.stage.requirement", "Requires %s");
    }
  }
}
