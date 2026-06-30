package com.herbcraft.alchemy.distillation;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Generic B3 distillation recipe: one base essence + orientation catalyst -> componentized distilled essence. */
public class DistillationRecipe extends CustomRecipe {
   public static final MapCodec<DistillationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(Codec.STRING.fieldOf("orientation").forGetter(recipe -> recipe.orientation))
         .apply(instance, DistillationRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      recipe -> recipe.orientation,
      DistillationRecipe::new
   );

   private final String orientation;

   public DistillationRecipe(String orientation) {
      this.orientation = orientation;
   }

   public String orientation() {
      return orientation;
   }

   public boolean matches(CraftingInput input, Level level) {
      return analyze(input).isPresent();
   }

   public ItemStack assemble(CraftingInput input) {
      return analyze(input).map(result -> DistillationItems.stack(result.sourceEssence(), result.orientation())).orElse(ItemStack.EMPTY);
   }

   private Optional<Result> analyze(CraftingInput input) {
      Optional<DistillationRules.Orientation> rule = DistillationRules.orientation(orientation).filter(DistillationRules.Orientation::implemented);
      if (rule.isEmpty()) {
         return Optional.empty();
      }
      String source = null;
      int catalysts = 0;
      int filled = 0;
      for (int i = 0; i < input.size(); i++) {
         ItemStack stack = input.getItem(i);
         if (stack.isEmpty()) {
            continue;
         }
         filled++;
         Optional<AlchemyRegistries.EssenceInfo> essence = AlchemyRegistries.essenceInfo(stack.getItem());
         if (essence.isPresent()) {
            if (source != null) {
               return Optional.empty();
            }
            source = essence.get().id();
         } else if (stack.is(rule.get().catalyst())) {
            catalysts++;
         } else {
            return Optional.empty();
         }
      }
      return filled == 2 && source != null && catalysts == 1 ? Optional.of(new Result(source, orientation)) : Optional.empty();
   }

   public RecipeSerializer<DistillationRecipe> getSerializer() {
      return DistillationRecipes.DISTILLATION;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   private record Result(String sourceEssence, String orientation) {
   }
}
