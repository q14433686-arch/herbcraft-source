package com.herbcraft.alchemy.potion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.alchemy.HerbcraftAlchemy;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AdvancedPotionStackInitializer {
   private static final int MAX_DURATION_TICKS = 9600;
   private static final List<AdvancedPotionStackInitializer.BonusRule> BONUS_RULES = new ArrayList<>();

   private AdvancedPotionStackInitializer() {
   }

   public static void loadBonusPoolsFromBundledJson() {
      BONUS_RULES.clear();
      try (InputStream input = AdvancedPotionStackInitializer.class.getClassLoader().getResourceAsStream("data/herbcraft_alchemy/potion_bonus_pools.json")) {
         if (input == null) {
            throw new IllegalStateException("Missing data/herbcraft_alchemy/potion_bonus_pools.json");
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         for (JsonElement element : root.getAsJsonArray("rules")) {
            BONUS_RULES.add(parseBonusRule(element.getAsJsonObject()));
         }
         HerbcraftAlchemy.LOGGER.info("Loaded {} advanced potion bonus pool rules.", BONUS_RULES.size());
      } catch (Exception e) {
         throw new RuntimeException("Failed to load herbcraft_alchemy potion_bonus_pools.json", e);
      }
   }

   private static AdvancedPotionStackInitializer.BonusRule parseBonusRule(JsonObject json) {
      String kind = json.has("kind") ? json.get("kind").getAsString() : "beneficial";
      boolean harmful = "harmful".equals(kind);
      boolean fallback = json.has("fallback") && json.get("fallback").getAsBoolean();
      List<Holder<MobEffect>> whenAny = new ArrayList<>();
      if (json.has("when_any")) {
         for (JsonElement element : json.getAsJsonArray("when_any")) {
            whenAny.add(effectHolder(element.getAsString(), "when_any"));
         }
      }
      List<AdvancedPotionStackInitializer.BonusSpec> pool = new ArrayList<>();
      for (JsonElement element : json.getAsJsonArray("pool")) {
         JsonObject effect = element.getAsJsonObject();
         int durationTicks = effect.has("duration_ticks") ? effect.get("duration_ticks").getAsInt() : effect.get("duration_seconds").getAsInt() * 20;
         int amplifier = effect.has("amplifier") ? effect.get("amplifier").getAsInt() : 0;
         pool.add(new AdvancedPotionStackInitializer.BonusSpec(effectHolder(effect.get("effect").getAsString(), "pool"), durationTicks, amplifier));
      }
      return new AdvancedPotionStackInitializer.BonusRule(harmful, fallback, List.copyOf(whenAny), List.copyOf(pool));
   }

   private static Holder<MobEffect> effectHolder(String id, String section) {
      return BuiltInRegistries.MOB_EFFECT
         .get(Identifier.parse(id))
         .map(holder -> (Holder<MobEffect>)holder)
         .orElseThrow(() -> new IllegalStateException("Unknown effect in potion_bonus_pools.json " + section + ": " + id));
   }

   public static boolean initializeIfNeeded(ItemStack stack, RandomSource random) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return false;
      }

      Optional<String> path = potionPath(contents);
      boolean advanced = path.filter(AdvancedPotionStackInitializer::isAdvancedPotionPath).isPresent();
      Optional<String> styleId = path.flatMap(PotionProcessingRules::processingStyleId);
      boolean implementedStyle = styleId.map(PotionProcessingRules::isImplemented).orElse(false);
      if (!advanced && !implementedStyle) {
         return false;
      }

      path.ifPresent(p -> stack.set(AlchemyPotionComponents.POTION_SOURCE, inferSourceId(p)));
      Boolean initialized = (Boolean)stack.get(AlchemyPotionComponents.ADVANCED_POTION_INITIALIZED);
      String currentStyle = (String)stack.get(AlchemyPotionComponents.POTION_PROCESSING_STYLE);
      boolean needsStyleRuntime = implementedStyle && styleId.filter(id -> !id.equals(currentStyle)).isPresent();
      boolean needsBurdenMetadata = styleId.isPresent() && stack.get(AlchemyPotionComponents.POTION_BURDEN) == null;
      if (Boolean.TRUE.equals(initialized) && !needsStyleRuntime && !needsBurdenMetadata) {
         return false;
      }

      boolean changed = false;
      AlchemyPotionComponents.PotionQuality quality = (AlchemyPotionComponents.PotionQuality)stack.get(AlchemyPotionComponents.POTION_QUALITY);
      if (advanced) {
         if (quality == null) {
            quality = AlchemyPotionComponents.PotionQuality.roll(random, path.map(AdvancedPotionStackInitializer::exceptionalBonus).orElse(0.0F));
            stack.set(AlchemyPotionComponents.POTION_QUALITY, quality);
            changed = true;
         }
         if (!Boolean.TRUE.equals(initialized)) {
            applyBonusIfNeeded(stack, contents, random);
            contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
               applyQuality(stack, contents, quality);
               changed = true;
            }
         }
      }

      contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      if (contents != null && needsStyleRuntime) {
         changed |= PotionProcessingRuntime.apply(stack, contents, styleId.get());
      }
      if (styleId.isPresent()) {
         changed |= PotionBurdenRuntime.applyMetadata(stack, styleId.get());
      }
      changed |= PotionPharmacologyProjectionRuntime.applyMetadata(stack);
      changed |= PotionBodyStateProjectionRuntime.applyMetadata(stack);

      stack.set(AlchemyPotionComponents.ADVANCED_POTION_INITIALIZED, Boolean.TRUE);
      return changed || needsStyleRuntime;
   }

   public static boolean isAdvanced(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      return contents != null && isAdvanced(contents);
   }

   public static boolean isAdvanced(PotionContents contents) {
      return advancedPotionPath(contents).isPresent();
   }

   public static Optional<String> advancedPotionPath(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      return contents == null ? Optional.empty() : advancedPotionPath(contents);
   }

   public static Optional<String> potionPath(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      return contents == null ? Optional.empty() : potionPath(contents);
   }

   public static Optional<String> potionPath(PotionContents contents) {
      Optional<String> potionPath = contents.potion()
         .flatMap(Holder::unwrapKey)
         .<Identifier>map(ResourceKey::identifier)
         .filter(id -> "herbcraft".equals(id.getNamespace()))
         .map(Identifier::getPath);
      return potionPath.isPresent() ? potionPath : contents.customName();
   }

   public static Optional<String> advancedPotionPath(PotionContents contents) {
      return potionPath(contents).filter(AdvancedPotionStackInitializer::isAdvancedPotionPath);
   }

   public static Optional<String> processingStyleId(ItemStack stack) {
      return potionPath(stack).flatMap(PotionProcessingRules::processingStyleId);
   }

   public static Optional<String> processingStyleId(PotionContents contents) {
      return potionPath(contents).flatMap(PotionProcessingRules::processingStyleId);
   }

   public static Optional<String> processingStyleId(String path) {
      return PotionProcessingRules.processingStyleId(path);
   }

   public static boolean isAdvancedPotionPath(String path) {
      return path.endsWith("_clarified")
         || path.endsWith("_murky")
         || path.equals("undead_venom")
         || path.equals("cardiac_toxin")
         || path.equals("wither_venom_iii");
   }

   private static String inferSourceId(String path) {
      String source = path;
      for (String suffix : new String[]{"_clarified", "_murky", "_long", "_strong"}) {
         if (source.endsWith(suffix)) {
            source = source.substring(0, source.length() - suffix.length());
         }
      }
      if ("undead_venom".equals(source)) {
         source = "flowering_azalea";
      } else if ("cardiac_toxin".equals(source)) {
         source = "lily_of_the_valley";
      } else if ("wither_venom_iii".equals(source)) {
         source = "wither_rose";
      }
      return "minecraft:" + source;
   }

   private static float exceptionalBonus(String path) {
      String sourceId = inferSourceId(path);
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(Identifier.parse(sourceId));
      if (path.endsWith("_murky") && (nature.traits().contains("fungal") || nature.traits().contains("toxic"))) {
         return 0.10F;
      }
      if (path.endsWith("_clarified") && (nature.traits().contains("nourishing") || nature.traits().contains("clearing"))) {
         return 0.10F;
      }
      return 0.0F;
   }

   public static String highTierTypeKey(String path) {
      if (path.endsWith("_clarified")) {
         return "clarified";
      } else if (!path.endsWith("_murky") && !path.equals("undead_venom") && !path.equals("cardiac_toxin") && !path.equals("wither_venom_iii")) {
         return "advanced";
      } else {
         return path.equals("wither_venom_iii") ? "t3" : "murky";
      }
   }

   private static void applyBonusIfNeeded(ItemStack stack, PotionContents contents, RandomSource random) {
      if (stack.get(AlchemyPotionComponents.POTION_BONUS) == null && !(random.nextFloat() >= 0.5F)) {
         String path = advancedPotionPath(contents).orElse("");
         List<AdvancedPotionStackInitializer.BonusSpec> pool = bonusPool(path, contents);
         if (!pool.isEmpty()) {
            AdvancedPotionStackInitializer.BonusSpec spec = pool.get(random.nextInt(pool.size()));
            Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)spec.effect().value());
            if (effectId != null) {
               PotionBonusComponent bonus = new PotionBonusComponent(effectId, spec.durationTicks(), spec.amplifier());
               stack.set(AlchemyPotionComponents.POTION_BONUS, bonus);
               stack.set(DataComponents.POTION_CONTENTS, contents.withEffectAdded(new MobEffectInstance(spec.effect(), spec.durationTicks(), spec.amplifier())));
            }
         }
      }
   }

   private static List<AdvancedPotionStackInitializer.BonusSpec> bonusPool(String path, PotionContents contents) {
      boolean harmful = "murky".equals(highTierTypeKey(path)) || "t3".equals(highTierTypeKey(path));
      List<AdvancedPotionStackInitializer.BonusSpec> fallback = List.of();
      for (AdvancedPotionStackInitializer.BonusRule rule : BONUS_RULES) {
         if (rule.harmful() != harmful) {
            continue;
         }
         if (rule.fallback()) {
            fallback = rule.pool();
         } else if (hasAny(contents, rule.whenAny())) {
            return rule.pool();
         }
      }
      return fallback;
   }

   private static boolean hasAny(PotionContents contents, List<Holder<MobEffect>> effects) {
      for (MobEffectInstance instance : contents.getAllEffects()) {
         for (Holder<MobEffect> effect : effects) {
            if (instance.is(effect)) {
               return true;
            }
         }
      }
      return false;
   }


   private static void applyQuality(ItemStack stack, PotionContents contents, AlchemyPotionComponents.PotionQuality quality) {
      float multiplier = quality.durationMultiplier();
      if (!(multiplier <= 1.0F) && !canUseVanillaDurationScale(contents, multiplier)) {
         List<MobEffectInstance> scaled = new ArrayList<>();

         for (MobEffectInstance effect : contents.getAllEffects()) {
            scaled.add(scaleAndCap(effect, multiplier));
         }

         Optional<String> customName = contents.customName()
            .or(() -> contents.potion().<ResourceKey>flatMap(Holder::unwrapKey).map(key -> key.identifier().getPath()));
         PotionContents rewritten = new PotionContents(Optional.empty(), contents.customColor(), List.copyOf(scaled), customName);
         stack.set(DataComponents.POTION_CONTENTS, rewritten);
         stack.set(DataComponents.POTION_DURATION_SCALE, 1.0F);
      } else {
         stack.set(DataComponents.POTION_DURATION_SCALE, multiplier);
      }
   }

   private static boolean canUseVanillaDurationScale(PotionContents contents, float multiplier) {
      for (MobEffectInstance effect : contents.getAllEffects()) {
         Holder<MobEffect> effectType = effect.getEffect();
         if (!((MobEffect)effectType.value()).isInstantaneous() && !effect.isInfiniteDuration() && Math.round(effect.getDuration() * multiplier) > 9600) {
            return false;
         }
      }

      return true;
   }

   private static MobEffectInstance scaleAndCap(MobEffectInstance effect, float multiplier) {
      Holder<MobEffect> effectType = effect.getEffect();
      if (!((MobEffect)effectType.value()).isInstantaneous() && !effect.isInfiniteDuration()) {
         int duration = Math.min(9600, Math.max(1, Math.round(effect.getDuration() * multiplier)));
         return new MobEffectInstance(effectType, duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
      } else {
         return new MobEffectInstance(effect);
      }
   }

   private record BonusRule(boolean harmful, boolean fallback, List<Holder<MobEffect>> whenAny, List<AdvancedPotionStackInitializer.BonusSpec> pool) {
   }

   private record BonusSpec(Holder<MobEffect> effect, int durationTicks, int amplifier) {
   }
}
