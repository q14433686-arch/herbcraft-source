package com.herbcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Minimal public wrapper around Minecraft's protected MobEffect constructor.
 *
 * <p>Phase 1 only registers Herbcraft's nutrition effect IDs, names and icons.
 * Gameplay behavior will be attached in the later NutritionManager migration pass.</p>
 */
public class SimpleHerbcraftMobEffect extends MobEffect {
   public SimpleHerbcraftMobEffect(MobEffectCategory category, int color) {
      super(category, color);
   }
}
