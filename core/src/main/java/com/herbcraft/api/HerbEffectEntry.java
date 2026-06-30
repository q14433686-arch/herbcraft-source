package com.herbcraft.api;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public record HerbEffectEntry(Holder<MobEffect> effect, int amplifier, int durationTicks, float chance, boolean negative) {
}
