package com.herbcraft.data;

import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.nutrition.NutritionProfile;
import com.herbcraft.nutrition.NutritionRegistry;
import com.herbcraft.knowledge.IngredientsChapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HerbcraftDataSyncPayload(
   Map<String, Map<String, Integer>> nutritionProfiles,
   Map<String, HerbcraftDataSyncPayload.NatureSync> natureProfiles,
   Map<String, String> ingredientEntryIds
) implements CustomPacketPayload {
   public static final Type<HerbcraftDataSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "data_sync"));
   public static final StreamCodec<RegistryFriendlyByteBuf, HerbcraftDataSyncPayload> STREAM_CODEC = CustomPacketPayload.codec(HerbcraftDataSyncPayload::write, HerbcraftDataSyncPayload::read);

   public static HerbcraftDataSyncPayload build() {
      Map<String, Map<String, Integer>> nutrition = new LinkedHashMap<>();
      NutritionRegistry.profiles().forEach((id, profile) -> nutrition.put(id.toString(), profile.values()));
      Map<String, NatureSync> natures = new LinkedHashMap<>();
      HerbNatureRegistry.profiles().forEach((id, profile) -> natures.put(id.toString(), NatureSync.from(profile)));
      Map<String, String> ingredients = new LinkedHashMap<>();
      IngredientsChapter.itemEntryIds().forEach((id, entryId) -> ingredients.put(id.toString(), entryId));
      return new HerbcraftDataSyncPayload(Map.copyOf(nutrition), Map.copyOf(natures), Map.copyOf(ingredients));
   }

   @Override
   public Type<HerbcraftDataSyncPayload> type() {
      return TYPE;
   }

   private void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(this.nutritionProfiles.size());
      this.nutritionProfiles.forEach((itemId, values) -> {
         buf.writeUtf(itemId);
         buf.writeVarInt(values.size());
         values.forEach((dimension, value) -> {
            buf.writeUtf(dimension);
            buf.writeVarInt(value);
         });
      });

      buf.writeVarInt(this.natureProfiles.size());
      this.natureProfiles.forEach((itemId, profile) -> {
         buf.writeUtf(itemId);
         profile.write(buf);
      });

      buf.writeVarInt(this.ingredientEntryIds.size());
      this.ingredientEntryIds.forEach((itemId, entryId) -> {
         buf.writeUtf(itemId);
         buf.writeUtf(entryId);
      });
   }

   private static HerbcraftDataSyncPayload read(RegistryFriendlyByteBuf buf) {
      int nutritionSize = buf.readVarInt();
      Map<String, Map<String, Integer>> nutrition = new LinkedHashMap<>();
      for (int i = 0; i < nutritionSize; i++) {
         String itemId = buf.readUtf();
         int valueCount = buf.readVarInt();
         Map<String, Integer> values = new LinkedHashMap<>();
         for (int j = 0; j < valueCount; j++) {
            values.put(buf.readUtf(), buf.readVarInt());
         }
         nutrition.put(itemId, Map.copyOf(values));
      }

      int natureSize = buf.readVarInt();
      Map<String, NatureSync> natures = new LinkedHashMap<>();
      for (int i = 0; i < natureSize; i++) {
         natures.put(buf.readUtf(), NatureSync.read(buf));
      }

      int ingredientSize = buf.readVarInt();
      Map<String, String> ingredients = new LinkedHashMap<>();
      for (int i = 0; i < ingredientSize; i++) {
         ingredients.put(buf.readUtf(), buf.readUtf());
      }
      return new HerbcraftDataSyncPayload(Map.copyOf(nutrition), Map.copyOf(natures), Map.copyOf(ingredients));
   }

   public record NatureSync(String temperature, List<String> flavors, List<String> tasteFlavors, List<String> traits) {
      public NatureSync {
         flavors = List.copyOf(flavors);
         tasteFlavors = List.copyOf(tasteFlavors);
         traits = List.copyOf(traits);
      }

      static NatureSync from(NatureProfile profile) {
         return new NatureSync(profile.temperature(), profile.flavors(), profile.tasteFlavors(), profile.traits());
      }

      NatureProfile toProfile() {
         return new NatureProfile(this.temperature, this.flavors, this.tasteFlavors, this.traits);
      }

      private void write(RegistryFriendlyByteBuf buf) {
         buf.writeUtf(this.temperature);
         writeStrings(buf, this.flavors);
         writeStrings(buf, this.tasteFlavors);
         writeStrings(buf, this.traits);
      }

      private static NatureSync read(RegistryFriendlyByteBuf buf) {
         return new NatureSync(buf.readUtf(), readStrings(buf), readStrings(buf), readStrings(buf));
      }

      private static void writeStrings(RegistryFriendlyByteBuf buf, List<String> values) {
         buf.writeVarInt(values.size());
         for (String value : values) {
            buf.writeUtf(value);
         }
      }

      private static List<String> readStrings(RegistryFriendlyByteBuf buf) {
         int size = buf.readVarInt();
         List<String> values = new ArrayList<>(size);
         for (int i = 0; i < size; i++) {
            values.add(buf.readUtf());
         }
         return List.copyOf(values);
      }
   }
}
