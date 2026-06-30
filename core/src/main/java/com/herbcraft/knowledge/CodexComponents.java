package com.herbcraft.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class CodexComponents {
   public static final DataComponentType<CodexComponents.PageEntry> PAGE_ENTRY = (DataComponentType<CodexComponents.PageEntry>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft", "page_entry"),
      DataComponentType.<CodexComponents.PageEntry>builder()
         .persistent(CodexComponents.PageEntry.CODEC)
         .networkSynchronized(CodexComponents.PageEntry.STREAM_CODEC)
         .cacheEncoding()
         .build()
   );
   public static final DataComponentType<PageKind> PAGE_KIND = (DataComponentType<PageKind>)Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      Identifier.fromNamespaceAndPath("herbcraft", "page_kind"),
      DataComponentType.<PageKind>builder()
         .persistent(PageKind.CODEC)
         .networkSynchronized(ByteBufCodecs.fromCodecWithRegistriesTrusted(PageKind.CODEC))
         .cacheEncoding()
         .build()
   );

   private CodexComponents() {
   }

   public static void initialize() {
   }

   public record PageEntry(String chapter, String entry) {
      public static final Codec<CodexComponents.PageEntry> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.STRING.optionalFieldOf("chapter", "").forGetter(CodexComponents.PageEntry::chapter),
               Codec.STRING.optionalFieldOf("entry", "").forGetter(CodexComponents.PageEntry::entry)
            )
            .apply(instance, CodexComponents.PageEntry::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, CodexComponents.PageEntry> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         CodexComponents.PageEntry::chapter,
         ByteBufCodecs.STRING_UTF8,
         CodexComponents.PageEntry::entry,
         CodexComponents.PageEntry::new
      );
   }
}
