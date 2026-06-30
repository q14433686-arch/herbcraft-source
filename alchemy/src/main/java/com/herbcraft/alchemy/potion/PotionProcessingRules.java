package com.herbcraft.alchemy.potion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data-backed processing-style registry for Herbcraft advanced potion paths.
 *
 * <p>This is a mechanism foundation only. The first pass deliberately maps the existing stable
 * potion path family to style identifiers without changing potion registry IDs or runtime effect
 * profiles. Later runtime layers can query these style IDs instead of hardcoding suffix tables.</p>
 */
public final class PotionProcessingRules {
   private static final String RESOURCE_PATH = "data/herbcraft_alchemy/potion_processing_rules.json";
   private static final Map<String, Style> STYLES = new LinkedHashMap<>();
   private static final Map<String, String> ALIASES = new LinkedHashMap<>();
   private static final Map<String, String> EXACT_PATHS = new LinkedHashMap<>();
   private static final List<SuffixMapping> SUFFIX_MAPPINGS = new ArrayList<>();
   private static final List<BurdenStage> BURDEN_STAGES = new ArrayList<>();
   private static final Map<String, BurdenConsequenceProfile> BURDEN_CONSEQUENCE_PROFILES = new LinkedHashMap<>();
   private static BurdenRuntime burdenRuntime = BurdenRuntime.DEFAULT;

   private PotionProcessingRules() {
   }

   public static void loadFromBundledJson() {
      STYLES.clear();
      ALIASES.clear();
      EXACT_PATHS.clear();
      SUFFIX_MAPPINGS.clear();
      BURDEN_STAGES.clear();
      BURDEN_CONSEQUENCE_PROFILES.clear();
      burdenRuntime = BurdenRuntime.DEFAULT;
      try (InputStream input = PotionProcessingRules.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
         if (input == null) {
            throw new IllegalStateException("Missing " + RESOURCE_PATH);
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         if (root.has("schema_version") && root.get("schema_version").getAsInt() != 1) {
            throw new IllegalStateException("Unsupported potion_processing_rules schema_version: " + root.get("schema_version"));
         }
         if (root.has("burden_runtime") && root.get("burden_runtime").isJsonObject()) {
            burdenRuntime = parseBurdenRuntime(root.getAsJsonObject("burden_runtime"));
         }
         if (root.has("burden_stages")) {
            for (JsonElement element : root.getAsJsonArray("burden_stages")) {
               BURDEN_STAGES.add(parseBurdenStage(element.getAsJsonObject()));
            }
         }
         if (root.has("burden_consequence_profiles") && root.get("burden_consequence_profiles").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("burden_consequence_profiles").entrySet()) {
               BURDEN_CONSEQUENCE_PROFILES.put(entry.getKey(), parseBurdenConsequenceProfile(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
         }
         if (BURDEN_STAGES.isEmpty()) {
            BURDEN_STAGES.add(BurdenStage.STEADY);
         }
         if (BURDEN_CONSEQUENCE_PROFILES.isEmpty()) {
            BURDEN_CONSEQUENCE_PROFILES.put("steady", BurdenConsequenceProfile.STEADY);
         }
         BURDEN_STAGES.sort(java.util.Comparator.comparingInt(BurdenStage::minTotal));
         for (JsonElement element : root.getAsJsonArray("styles")) {
            Style style = parseStyle(element.getAsJsonObject());
            STYLES.put(style.id(), style);
            ALIASES.put(style.id(), style.id());
            style.aliases().forEach(alias -> ALIASES.put(alias, style.id()));
            style.exactPaths().forEach(path -> EXACT_PATHS.put(path, style.id()));
            style.pathSuffixes().forEach(suffix -> SUFFIX_MAPPINGS.add(new SuffixMapping(suffix, style.id())));
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} potion processing styles.", STYLES.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy potion_processing_rules.json", e);
      }
   }

   private static Style parseStyle(JsonObject json) {
      String id = string(json, "id", "");
      if (id.isBlank()) {
         throw new IllegalStateException("Potion processing style missing id");
      }
      return new Style(
         id,
         bool(json, "implemented", false),
         bool(json, "first_batch", false),
         bool(json, "deferred", false),
         list(json, "aliases"),
         list(json, "path_suffixes"),
         list(json, "exact_paths"),
         string(json, "burden_profile", "none"),
         string(json, "thermal_shift", "neutral"),
         string(json, "qi_bias", "neutral"),
         string(json, "clarity_level", "standard"),
         string(json, "relation_modifier", "neutral"),
         list(json, "flavor_bias"),
         list(json, "trait_affinity"),
         string(json, "lang_key", "tooltip.herbcraft.potion.processing." + id),
         string(json, "burden_hint_key", "tooltip.herbcraft.potion.burden_profile.none"),
         string(json, "thermal_hint_key", "tooltip.herbcraft.potion.thermal_shift.neutral"),
         string(json, "qi_hint_key", "tooltip.herbcraft.potion.qi_bias.neutral"),
         string(json, "clarity_hint_key", "tooltip.herbcraft.potion.clarity_level.standard"),
         string(json, "relation_hint_key", "tooltip.herbcraft.potion.relation_modifier.neutral"),
         i(json, "burden_weight", 0),
         i(json, "tail_weight", 0),
         parseRuntime(json)
      );
   }

   public static Optional<Style> style(String idOrAlias) {
      if (idOrAlias == null || idOrAlias.isBlank()) {
         return Optional.empty();
      }
      String id = ALIASES.getOrDefault(idOrAlias, idOrAlias);
      return Optional.ofNullable(STYLES.get(id));
   }

   public static List<Style> styles() {
      return List.copyOf(STYLES.values());
   }

   public static Optional<String> processingStyleId(String potionPath) {
      if (potionPath == null || potionPath.isBlank()) {
         return Optional.empty();
      }
      String exact = EXACT_PATHS.get(potionPath);
      if (exact != null) {
         return Optional.of(exact);
      }
      for (SuffixMapping mapping : SUFFIX_MAPPINGS) {
         if (!mapping.suffix().isBlank() && potionPath.endsWith(mapping.suffix())) {
            return Optional.of(mapping.styleId());
         }
      }
      return Optional.empty();
   }

   public static Optional<RuntimeProfile> runtime(String idOrAlias) {
      return style(idOrAlias).map(Style::runtime);
   }

   public static BurdenRuntime burdenRuntime() {
      return burdenRuntime;
   }

   public static List<BurdenStage> burdenStages() {
      return List.copyOf(BURDEN_STAGES);
   }

   public static BurdenStage burdenStageFor(int totalBurden) {
      BurdenStage result = BURDEN_STAGES.isEmpty() ? BurdenStage.STEADY : BURDEN_STAGES.get(0);
      for (BurdenStage stage : BURDEN_STAGES) {
         if (totalBurden >= stage.minTotal()) {
            result = stage;
         }
      }
      return result;
   }

   public static BurdenConsequenceProfile burdenConsequenceProfile(String profileId) {
      return BURDEN_CONSEQUENCE_PROFILES.getOrDefault(profileId, BURDEN_CONSEQUENCE_PROFILES.getOrDefault("steady", BurdenConsequenceProfile.STEADY));
   }

   public static List<BurdenConsequenceProfile> burdenConsequenceProfiles() {
      return List.copyOf(BURDEN_CONSEQUENCE_PROFILES.values());
   }

   public static boolean isImplemented(String idOrAlias) {
      return style(idOrAlias).map(Style::implemented).orElse(false);
   }

   public static boolean isDeferred(String idOrAlias) {
      return style(idOrAlias).map(Style::deferred).orElse(false);
   }

   private static BurdenStage parseBurdenStage(JsonObject json) {
      return new BurdenStage(
         string(json, "id", "steady"),
         i(json, "min_total", 0),
         f(json, "speed_modifier", 0.0F),
         string(json, "message_key", ""),
         string(json, "effect", ""),
         i(json, "effect_duration_ticks", 240)
      );
   }

   private static BurdenConsequenceProfile parseBurdenConsequenceProfile(String id, JsonObject json) {
      return new BurdenConsequenceProfile(
         id,
         string(json, "effect", ""),
         f(json, "strain_duration_multiplier", 1.0F),
         f(json, "overload_duration_multiplier", 1.0F),
         f(json, "exhaustion_per_tick", 0.0F),
         string(json, "message_key", "")
      );
   }

   private static BurdenRuntime parseBurdenRuntime(JsonObject json) {
      return new BurdenRuntime(
         i(json, "decay_interval_ticks", BurdenRuntime.DEFAULT.decayIntervalTicks()),
         i(json, "active_decay_amount", BurdenRuntime.DEFAULT.activeDecayAmount()),
         i(json, "tail_decay_amount", BurdenRuntime.DEFAULT.tailDecayAmount()),
         i(json, "max_burden", BurdenRuntime.DEFAULT.maxBurden())
      );
   }

   private static RuntimeProfile parseRuntime(JsonObject json) {
      if (!json.has("runtime") || !json.get("runtime").isJsonObject()) {
         return RuntimeProfile.NONE;
      }
      JsonObject runtime = json.getAsJsonObject("runtime");
      return new RuntimeProfile(
         f(runtime, "duration_multiplier", 1.0F),
         i(runtime, "amplifier_delta", 0),
         i(runtime, "max_amplifier", 255),
         bool(runtime, "force_non_ambient", false),
         bool(runtime, "force_visible", false),
         bool(runtime, "force_icon", false)
      );
   }

   private static boolean bool(JsonObject json, String key, boolean fallback) {
      return json.has(key) ? json.get(key).getAsBoolean() : fallback;
   }

   private static int i(JsonObject json, String key, int fallback) {
      return json.has(key) ? json.get(key).getAsInt() : fallback;
   }

   private static float f(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }

   private static String string(JsonObject json, String key, String fallback) {
      return json.has(key) ? json.get(key).getAsString() : fallback;
   }

   private static List<String> list(JsonObject json, String key) {
      if (!json.has(key)) {
         return List.of();
      }
      List<String> values = new ArrayList<>();
      for (JsonElement element : json.getAsJsonArray(key)) {
         String value = element.getAsString();
         if (!value.isBlank()) {
            values.add(value);
         }
      }
      return List.copyOf(values);
   }

   public record Style(
      String id,
      boolean implemented,
      boolean firstBatch,
      boolean deferred,
      List<String> aliases,
      List<String> pathSuffixes,
      List<String> exactPaths,
      String burdenProfile,
      String thermalShift,
      String qiBias,
      String clarityLevel,
      String relationModifier,
      List<String> flavorBias,
      List<String> traitAffinity,
      String langKey,
      String burdenHintKey,
      String thermalHintKey,
      String qiHintKey,
      String clarityHintKey,
      String relationHintKey,
      int burdenWeight,
      int tailWeight,
      RuntimeProfile runtime
   ) {
   }

   public record RuntimeProfile(
      float durationMultiplier,
      int amplifierDelta,
      int maxAmplifier,
      boolean forceNonAmbient,
      boolean forceVisible,
      boolean forceIcon
   ) {
      public static final RuntimeProfile NONE = new RuntimeProfile(1.0F, 0, 255, false, false, false);
   }

   public record BurdenRuntime(int decayIntervalTicks, int activeDecayAmount, int tailDecayAmount, int maxBurden) {
      public static final BurdenRuntime DEFAULT = new BurdenRuntime(200, 5, 2, 1000);
   }

   public record BurdenStage(String id, int minTotal, float speedModifier, String messageKey, String effect, int effectDurationTicks) {
      public static final BurdenStage STEADY = new BurdenStage("steady", 0, 0.0F, "", "", 240);
   }

   public record BurdenConsequenceProfile(
      String id,
      String effect,
      float strainDurationMultiplier,
      float overloadDurationMultiplier,
      float exhaustionPerTick,
      String messageKey
   ) {
      public static final BurdenConsequenceProfile STEADY = new BurdenConsequenceProfile("steady", "", 1.0F, 1.0F, 0.0F, "");
   }

   private record SuffixMapping(String suffix, String styleId) {
   }
}
