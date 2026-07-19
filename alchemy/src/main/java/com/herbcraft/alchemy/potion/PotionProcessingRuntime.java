package com.herbcraft.alchemy.potion;

import com.herbcraft.alchemy.AlchemyExpansionEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/** Applies first-batch potion processing style behavior to existing stable potion paths. */
public final class PotionProcessingRuntime {
   private static final int MAX_DURATION_TICKS = 9600;

   private PotionProcessingRuntime() {
   }

   public static boolean apply(ItemStack stack, PotionContents contents, String styleId) {
      Optional<PotionProcessingRules.Style> style = PotionProcessingRules.style(styleId);
      if (style.isEmpty() || !style.get().implemented() || style.get().deferred()) {
         return false;
      }
      PotionProcessingRules.RuntimeProfile runtime = style.get().runtime();
      List<MobEffectInstance> rewritten = new ArrayList<>();
      boolean changed = false;
      for (MobEffectInstance effect : contents.getAllEffects()) {
         MobEffectInstance transformed = transform(effect, runtime);
         rewritten.add(transformed);
         changed |= !sameEffectProfile(effect, transformed);
      }
      if (!changed) {
         stack.set(AlchemyPotionComponents.POTION_PROCESSING_STYLE, style.get().id());
         return false;
      }
      Optional<String> customName = contents.customName()
         .or(() -> contents.potion().flatMap(Holder::unwrapKey).map(ResourceKey::identifier).map(id -> id.getPath()));
      PotionContents processed = new PotionContents(Optional.empty(), contents.customColor(), List.copyOf(rewritten), customName);
      stack.set(DataComponents.POTION_CONTENTS, processed);
      stack.set(DataComponents.POTION_DURATION_SCALE, 1.0F);
      stack.set(AlchemyPotionComponents.POTION_PROCESSING_STYLE, style.get().id());
      AlchemyExpansionEvents.PROCESSING_STYLE_APPLIED.invoker().onProcessingStyleApplied(stack, style.get().id());
      return true;
   }

   private static MobEffectInstance transform(MobEffectInstance effect, PotionProcessingRules.RuntimeProfile runtime) {
      Holder<MobEffect> effectType = effect.getEffect();
      int amplifier = Math.max(0, Math.min(runtime.maxAmplifier(), effect.getAmplifier() + runtime.amplifierDelta()));
      int duration = effect.getDuration();
      if (!((MobEffect)effectType.value()).isInstantaneous() && !effect.isInfiniteDuration()) {
         duration = Math.min(MAX_DURATION_TICKS, Math.max(1, Math.round(duration * runtime.durationMultiplier())));
      }
      boolean ambient = runtime.forceNonAmbient() ? false : effect.isAmbient();
      boolean visible = runtime.forceVisible() || effect.isVisible();
      boolean icon = runtime.forceIcon() || effect.showIcon();
      return new MobEffectInstance(effectType, duration, amplifier, ambient, visible, icon);
   }

   private static boolean sameEffectProfile(MobEffectInstance left, MobEffectInstance right) {
      return left.getEffect().equals(right.getEffect())
         && left.getDuration() == right.getDuration()
         && left.getAmplifier() == right.getAmplifier()
         && left.isAmbient() == right.isAmbient()
         && left.isVisible() == right.isVisible()
         && left.showIcon() == right.showIcon();
   }
}
