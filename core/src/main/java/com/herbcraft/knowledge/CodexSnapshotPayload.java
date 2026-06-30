package com.herbcraft.knowledge;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.nature.NatureProfile;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public record CodexSnapshotPayload(List<CodexSnapshotPayload.ChapterSnap> chapters) implements CustomPacketPayload {
   public static final Type<CodexSnapshotPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "codex_snapshot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CodexSnapshotPayload> STREAM_CODEC = CustomPacketPayload.codec(CodexSnapshotPayload::write, CodexSnapshotPayload::read);
   public Type<CodexSnapshotPayload> type() { return TYPE; }
   public static CodexSnapshotPayload build(ServerPlayer player) {
      List<ChapterSnap> chapters = new ArrayList<>();
      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         Map<String,List<EntrySnap>> bySection = new LinkedHashMap<>(); chapter.sections().keySet().forEach(s->bySection.put(s,new ArrayList<>())); bySection.put("",new ArrayList<>());
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            KnowledgeDisplayState display = KnowledgeAPI.displayState(player, chapter.id(), entry);
            List<Component> lines = display == KnowledgeDisplayState.UNKNOWN ? List.of() : entry.lines().apply(player, KnowledgeAPI.state(player, chapter.id(), entry));
            String key = chapter.id()+"/"+entry.id();
            List<ClaimSnap> claims = claimSnaps(player, key, lines);
            // Resolve item ID for this entry so client can render icons
            String itemId = resolveEntryItemId(chapter.id(), entry.id());
            // Resolve nature data for tasted/mastered entries
            String temperature = "";
            String flavors = "";
            if (display.ordinal() >= KnowledgeDisplayState.TASTED.ordinal() && !itemId.isEmpty()) {
               Identifier itemIdentifier = Identifier.tryParse(itemId);
               if (itemIdentifier != null) {
                  NatureProfile nature = HerbNatureAPI.getNatureOrFallback(itemIdentifier);
                  temperature = nature.temperature();
                  flavors = String.join(",", nature.tasteFlavors());
               }
            }
            bySection.computeIfAbsent(entry.section()==null?"":entry.section(), k->new ArrayList<>()).add(new EntrySnap(key, entry.title(), display.ordinal(), lines, claims, itemId, temperature, flavors));
         }
         List<SectionSnap> sections=new ArrayList<>(); chapter.sections().forEach((sid,title)->{List<EntrySnap> es=bySection.getOrDefault(sid,List.of()); if(!es.isEmpty()) sections.add(new SectionSnap(title,es));}); List<EntrySnap> un=bySection.getOrDefault("",List.of()); if(!un.isEmpty()) sections.add(new SectionSnap(Component.translatable("book.herbcraft.section.general"),un)); chapters.add(new ChapterSnap(chapter.title(),sections));
      }
      return new CodexSnapshotPayload(chapters);
   }

   /**
    * Resolve the Minecraft item ID string for a knowledge entry.
    * For herbal chapter herbs: entryId is the item path -> "minecraft:<entryId>"
    * For food-nature entries: entryId is "namespace__path" -> "namespace:path"
    * For ingredient chapter entries: similar to herbal mapping
    * For alchemy chapter essences: "essence_xyz" -> "herbcraft:essence_xyz"
    * For cuisine chapter dishes: "dish_id" -> "herbcraft_cuisine:dish_id"
    * For guide/overview entries: returns empty string (no item icon).
    */
   private static String resolveEntryItemId(String chapterId, String entryId) {
      if ("overview".equals(entryId) || entryId.startsWith("guide_")) {
         return "";
      }
      if ("herbal".equals(chapterId)) {
         return resolveHerbalItemId(entryId);
      }
      if ("ingredients".equals(chapterId)) {
         return resolveNamespaceAwareItemId(entryId);
      }
      if ("alchemy".equals(chapterId)) {
         return resolveAlchemyItemId(entryId);
      }
      if ("cuisine".equals(chapterId)) {
         return resolveCuisineItemId(entryId);
      }
      return "";
   }

   private static String resolveHerbalItemId(String entryId) {
      // Food-nature entries use "namespace__path" format
      if (entryId.contains("__")) {
         return resolveNamespaceAwareItemId(entryId);
      }
      // Regular herb: entryId = item path, try minecraft: first, then herbcraft:
      Identifier id = Identifier.fromNamespaceAndPath("minecraft", entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      id = Identifier.fromNamespaceAndPath("herbcraft", entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      // Robust search across all registered items for a matching path
      for (Identifier identifier : BuiltInRegistries.ITEM.keySet()) {
         if (identifier.getPath().equals(entryId)) {
            return identifier.toString();
         }
      }
      return "";
   }

   private static String resolveNamespaceAwareItemId(String entryId) {
      if (entryId.contains("__")) {
         int sep = entryId.indexOf("__");
         String ns = entryId.substring(0, sep);
         String path = entryId.substring(sep + 2);
         Identifier id = Identifier.fromNamespaceAndPath(ns, path);
         if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
         return "";
      }
      Identifier id = Identifier.fromNamespaceAndPath("minecraft", entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      return "";
   }

   private static String resolveAlchemyItemId(String entryId) {
      // Essence entries: "essence_xyz" → "herbcraft:essence_xyz"
      if (entryId.startsWith("essence_")) {
         Identifier id = Identifier.fromNamespaceAndPath("herbcraft", entryId);
         if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      }
      // Potion entries: "potion_xyz" → potion items are parameterized, no simple item icon
      // Coating entries: "coating_xyz" → adhesive items, try "herbcraft:<entryId>"
      if (entryId.startsWith("coating_")) {
         // Try the adhesive item pattern: herbcraft:echo_adhesive etc.
         // Coating names don't map 1:1 to adhesive items, skip for now
         return "";
      }
      // Fallback: try herbcraft namespace
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft", entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      return "";
   }

   private static String resolveCuisineItemId(String entryId) {
      // Cuisine dish entries: "dandelion_salad" → "herbcraft_cuisine:dandelion_salad"
      Identifier id = Identifier.fromNamespaceAndPath("herbcraft_cuisine", entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      // Some cuisine entries might have raw_ prefix
      id = Identifier.fromNamespaceAndPath("herbcraft_cuisine", "raw_" + entryId);
      if (BuiltInRegistries.ITEM.containsKey(id)) return id.toString();
      return "";
   }

   private static List<ClaimSnap> claimSnaps(ServerPlayer player, String entryKey, List<Component> lines) {
      List<ClaimSnap> result = new ArrayList<>();
      List<KnowledgeClaim> claims = KnowledgeRumorRegistry.claims(entryKey);
      int searchFrom = 0;
      for (KnowledgeClaim claim : claims) {
         int mark = KnowledgeAPI.claimMark(player, entryKey, claim.id()).ordinal();
         int lineIndex = findClaimLine(lines, searchFrom);
         if (lineIndex >= 0) {
            result.add(new ClaimSnap(entryKey, claim.id(), mark, lineIndex));
            searchFrom = lineIndex + 1;
         }
      }
      return result;
   }

   private static int findClaimLine(List<Component> lines, int start) {
      for (int i = Math.max(0, start); i < lines.size(); i++) {
         String text = lines.get(i).getString().trim();
         if (text.startsWith("○") || text.startsWith("✓") || text.startsWith("✕")) {
            return i;
         }
      }
      return -1;
   }

   private void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(chapters.size());
      for (ChapterSnap c : chapters) {
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, c.title());
         buf.writeVarInt(c.sections().size());
         for (SectionSnap s : c.sections()) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, s.title());
            buf.writeVarInt(s.entries().size());
            for (EntrySnap e : s.entries()) {
               buf.writeUtf(e.entryKey());
               ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, e.title());
               buf.writeVarInt(e.state());
               buf.writeVarInt(e.lines().size());
               for (Component l : e.lines()) ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, l);
               buf.writeVarInt(e.claims().size());
               for (ClaimSnap cl : e.claims()) {
                  buf.writeUtf(cl.entryKey());
                  buf.writeUtf(cl.claimId());
                  buf.writeVarInt(cl.mark());
                  buf.writeVarInt(cl.lineIndex());
               }
               // New Layer 2 fields
               buf.writeUtf(e.itemId());
               buf.writeUtf(e.temperature());
               buf.writeUtf(e.flavors());
            }
         }
      }
   }

   private static CodexSnapshotPayload read(RegistryFriendlyByteBuf buf) {
      int cc = buf.readVarInt();
      List<ChapterSnap> ch = new ArrayList<>(cc);
      for (int i = 0; i < cc; i++) {
         Component ct = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
         int sc = buf.readVarInt();
         List<SectionSnap> sections = new ArrayList<>(sc);
         for (int j = 0; j < sc; j++) {
            Component st = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
            int ec = buf.readVarInt();
            List<EntrySnap> entries = new ArrayList<>(ec);
            for (int k = 0; k < ec; k++) {
               String key = buf.readUtf();
               Component et = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
               int state = buf.readVarInt();
               int lc = buf.readVarInt();
               List<Component> lines = new ArrayList<>(lc);
               for (int l = 0; l < lc; l++) lines.add(ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf));
               int clc = buf.readVarInt();
               List<ClaimSnap> claims = new ArrayList<>(clc);
               for (int m = 0; m < clc; m++) claims.add(new ClaimSnap(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt()));
               // New Layer 2 fields
               String itemId = buf.readUtf();
               String temperature = buf.readUtf();
               String flavors = buf.readUtf();
               entries.add(new EntrySnap(key, et, state, lines, claims, itemId, temperature, flavors));
            }
            sections.add(new SectionSnap(st, entries));
         }
         ch.add(new ChapterSnap(ct, sections));
      }
      return new CodexSnapshotPayload(ch);
   }

   public record ChapterSnap(Component title, List<SectionSnap> sections) {}
   public record EntrySnap(String entryKey, Component title, int state, List<Component> lines, List<ClaimSnap> claims, String itemId, String temperature, String flavors) {}
   public record SectionSnap(Component title, List<EntrySnap> entries) {}
   public record ClaimSnap(String entryKey, String claimId, int mark, int lineIndex) {}
}
