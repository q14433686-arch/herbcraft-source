package com.herbcraft.alchemy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class HerbcraftAlchemyConfig {
   private static final Gson GSON = new Gson();
   private static boolean loaded;
   private static boolean enableWitchHerbcraftPotions = true;
   private static float witchHerbcraftPotionChance = 0.10F;

   private HerbcraftAlchemyConfig() {}

   public static void load() {
      if (loaded) return;
      loaded = true;
      Path path = FabricLoader.getInstance().getConfigDir().resolve("herbcraft-alchemy.json");
      try {
         if (Files.exists(path)) {
            JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (json.has("enable_witch_herbcraft_potions")) enableWitchHerbcraftPotions = json.get("enable_witch_herbcraft_potions").getAsBoolean();
            if (json.has("witch_herbcraft_potion_chance")) witchHerbcraftPotionChance = Math.max(0.0F, Math.min(1.0F, json.get("witch_herbcraft_potion_chance").getAsFloat()));
         } else {
            writeDefault(path);
         }
      } catch (Exception ignored) {
         writeDefault(path);
      }
   }

   public static boolean enableWitchHerbcraftPotions() {
      load();
      return enableWitchHerbcraftPotions;
   }

   public static float witchHerbcraftPotionChance() {
      load();
      return witchHerbcraftPotionChance;
   }

   private static void writeDefault(Path path) {
      try {
         Files.createDirectories(path.getParent());
         JsonObject json = new JsonObject();
         json.addProperty("enable_witch_herbcraft_potions", enableWitchHerbcraftPotions);
         json.addProperty("witch_herbcraft_potion_chance", witchHerbcraftPotionChance);
         Files.writeString(path, GSON.toJson(json), StandardCharsets.UTF_8);
      } catch (Exception ignored) {}
   }
}
