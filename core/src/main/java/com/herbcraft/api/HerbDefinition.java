package com.herbcraft.api;

import java.util.List;
import net.minecraft.world.item.Item;

public record HerbDefinition(
   Item item, int nutrition, float saturation, float consumeSeconds, boolean medicinal, boolean stackGroup, List<HerbEffectEntry> effects
) {
}
