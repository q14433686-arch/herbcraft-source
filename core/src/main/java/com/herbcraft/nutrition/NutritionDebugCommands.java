package com.herbcraft.nutrition;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class NutritionDebugCommands {
   private static final List<String> DIMENSIONS = List.of("grain", "flesh", "oil", "vege", "fruit", "all");
   private static final List<String> PRESETS = List.of(
      "starved",
      "balanced",
      "grain_low",
      "flesh_low",
      "oil_low",
      "vege_low",
      "fruit_low",
      "imbalanced_grain",
      "imbalanced_flesh",
      "imbalanced_oil",
      "imbalanced_vege",
      "imbalanced_fruit",
      "excess_flesh",
      "excess_oil",
      "excess_fruit",
      "correcting_flesh",
      "correcting_oil",
      "correcting_grain",
      "excess_fruit_nonpunitive"
   );
   private static final SuggestionProvider<CommandSourceStack> DIMENSION_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(DIMENSIONS, builder);
   private static final SuggestionProvider<CommandSourceStack> PRESET_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(PRESETS, builder);

   private NutritionDebugCommands() {
   }

   public static void register() {
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
         Commands.literal("herbcraft")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("nutrition")
               .then(Commands.literal("get")
                  .executes(context -> getSelf(context))
                  .then(Commands.argument("target", EntityArgument.player())
                     .executes(context -> getOne(context, EntityArgument.getPlayer(context, "target")))))
               .then(Commands.literal("set")
                  .then(Commands.argument("targets", EntityArgument.players())
                     .then(Commands.argument("dimension", StringArgumentType.word())
                        .suggests(DIMENSION_SUGGESTIONS)
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 1000))
                           .executes(NutritionDebugCommands::set)))))
               .then(Commands.literal("preset")
                  .then(Commands.argument("targets", EntityArgument.players())
                     .then(Commands.argument("preset", StringArgumentType.word())
                        .suggests(PRESET_SUGGESTIONS)
                        .executes(NutritionDebugCommands::preset))))
            )
      ));
   }

   private static int getSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
      return getOne(context, context.getSource().getPlayerOrException());
   }

   private static int getOne(CommandContext<CommandSourceStack> context, ServerPlayer player) {
      context.getSource().sendSuccess(() -> statusLine(player), false);
      return 1;
   }

   private static int set(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
      Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
      String dimension = StringArgumentType.getString(context, "dimension");
      int value = IntegerArgumentType.getInteger(context, "value");
      if (!DIMENSIONS.contains(dimension)) {
         context.getSource().sendFailure(Component.literal("Unknown nutrition dimension: " + dimension));
         return 0;
      }
      for (ServerPlayer player : targets) {
         NutritionManager.setNutritionForDebug(player, dimension, value);
      }
      context.getSource().sendSuccess(
         () -> Component.literal("Set Herbcraft nutrition " + dimension + "=" + value + " for " + targets.size() + " player(s)."),
         true
      );
      return targets.size();
   }

   private static int preset(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
      Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
      String preset = StringArgumentType.getString(context, "preset");
      if (!PRESETS.contains(preset)) {
         context.getSource().sendFailure(Component.literal("Unknown nutrition preset: " + preset));
         return 0;
      }
      for (ServerPlayer player : targets) {
         NutritionManager.applyNutritionPresetForDebug(player, preset);
      }
      context.getSource().sendSuccess(
         () -> Component.literal("Applied Herbcraft nutrition preset " + preset + " to " + targets.size() + " player(s)."),
         true
      );
      return targets.size();
   }

   private static Component statusLine(ServerPlayer player) {
      Map<String, Integer> values = NutritionManager.nutritionState(player).snapshot();
      return Component.literal(
         "Herbcraft nutrition for " + player.getName().getString()
            + ": grain=" + values.getOrDefault("grain", 0)
            + ", flesh=" + values.getOrDefault("flesh", 0)
            + ", oil=" + values.getOrDefault("oil", 0)
            + ", vege=" + values.getOrDefault("vege", 0)
            + ", fruit=" + values.getOrDefault("fruit", 0)
      );
   }
}
