package lofimodding.progression.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import lofimodding.progression.Stage;
import lofimodding.progression.capabilities.ProgressCapability;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public final class StageCommand {
  private StageCommand() { }

  public static ArgumentBuilder<CommandSource, ?> build() {
    return Commands.literal("stage")
      .requires(ctx -> ctx.hasPermissionLevel(4))
      .then(
        Commands.literal("get")
          .then(
            Commands.argument("targets", EntityArgument.players())
              .executes(ctx -> get(ctx, EntityArgument.getPlayers(ctx, "targets")))
          )
      )
      .then(
        Commands.literal("grant")
          .then(
            Commands.argument("targets", EntityArgument.players())
              .then(
                Commands.argument("stage", ResourceLocationArgument.resourceLocation())
                  .executes(ctx -> grant(ctx, EntityArgument.getPlayers(ctx, "targets"), ResourceLocationArgument.getResourceLocation(ctx, "stage")))
              )
          )
      )
      .then(
        Commands.literal("revoke")
          .then(
            Commands.argument("targets", EntityArgument.players())
              .then(
                Commands.argument("stage", ResourceLocationArgument.resourceLocation())
                  .executes(ctx -> revoke(ctx, EntityArgument.getPlayers(ctx, "targets"), ResourceLocationArgument.getResourceLocation(ctx, "stage")))
              )
          )
      )
      ;
  }

  private static int get(final CommandContext<CommandSource> ctx, final Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
    final ServerPlayerEntity sender = ctx.getSource().asPlayer();

    for(final ServerPlayerEntity target : players) {
      target
        .getCapability(ProgressCapability.CAPABILITY)
        .ifPresent(progress -> {
          if(progress.getStages().isEmpty()) {
            sender.sendMessage(new TranslationTextComponent("commands.stage.get.no_stages", target.getDisplayName()));
          } else {
            sender.sendMessage(new TranslationTextComponent("commands.stage.get.stages_for", target.getDisplayName()));

            for(final Stage stage : progress.getStages()) {
              sender.sendMessage(new TranslationTextComponent("commands.stage.get.stages_list", stage.getRegistryName()));
            }
          }
        });
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int grant(final CommandContext<CommandSource> ctx, final Collection<ServerPlayerEntity> players, final ResourceLocation id) throws CommandSyntaxException {
    final Stage stage = Stage.REGISTRY.get().getValue(id);

    if(stage == null) {
      throw new SimpleCommandExceptionType(new TranslationTextComponent("commands.stage.grant.invalid_stage", id)).create();
    }

    final ServerPlayerEntity sender = ctx.getSource().asPlayer();

    for(final ServerPlayerEntity target : players) {
      target
        .getCapability(ProgressCapability.CAPABILITY)
        .ifPresent(progress -> {
          progress.grantStage(stage);
          sender.sendMessage(new TranslationTextComponent("commands.stage.grant.granted_to", target.getDisplayName(), stage.getRegistryName()));

          if(!sender.equals(target)) {
            target.sendMessage(new TranslationTextComponent("commands.stage.grant.granted", stage.getRegistryName()));
          }
        });
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int revoke(final CommandContext<CommandSource> ctx, final Collection<ServerPlayerEntity> players, final ResourceLocation id) throws CommandSyntaxException {
    final Stage stage = Stage.REGISTRY.get().getValue(id);

    if(stage == null) {
      throw new SimpleCommandExceptionType(new TranslationTextComponent("commands.stage.revoke.invalid_stage", id)).create();
    }

    final ServerPlayerEntity sender = ctx.getSource().asPlayer();

    for(final ServerPlayerEntity target : players) {
      target
        .getCapability(ProgressCapability.CAPABILITY)
        .ifPresent(progress -> {
          progress.grantStage(stage);
          sender.sendMessage(new TranslationTextComponent("commands.stage.revoke.revoked_to", target.getDisplayName(), stage.getRegistryName()));

          if(!sender.equals(target)) {
            target.sendMessage(new TranslationTextComponent("commands.stage.revoke.revoked", stage.getRegistryName()));
          }
        });
    }

    return Command.SINGLE_SUCCESS;
  }
}
