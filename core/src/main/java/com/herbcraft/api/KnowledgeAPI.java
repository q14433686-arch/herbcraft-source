package com.herbcraft.api;

import com.herbcraft.HerbcraftPlayerData;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public final class KnowledgeAPI {
   private static final Map<String, KnowledgeAPI.Chapter> CHAPTERS = new LinkedHashMap<>();
   private static long registrationCounter = 0L;
   private KnowledgeAPI() {}
   public static synchronized KnowledgeAPI.Chapter registerChapter(String id, Component title, int sortPriority) { return CHAPTERS.computeIfAbsent(id, k -> new KnowledgeAPI.Chapter(k, title, sortPriority, registrationCounter++)); }
   public static synchronized KnowledgeAPI.Chapter registerChapter(String id, Component title) { return registerChapter(id, title, 1000); }
   public static synchronized void registerSection(String chapterId, String sectionId, Component title) { Chapter c=CHAPTERS.get(chapterId); if(c==null) throw new IllegalStateException("Unknown knowledge chapter: "+chapterId); c.sections.putIfAbsent(sectionId,title); }
   public static synchronized void registerEntry(String chapterId, Entry entry) { Chapter c=CHAPTERS.get(chapterId); if(c==null) throw new IllegalStateException("Unknown knowledge chapter: "+chapterId); if(entry.section()!=null&&!c.sections.containsKey(entry.section())) throw new IllegalStateException("Unknown section "+entry.section()+" in chapter "+chapterId); c.entries.put(entry.id(),entry); }
   public static synchronized void removeEntry(String chapterId,String entryId){ Chapter c=CHAPTERS.get(chapterId); if(c!=null)c.entries.remove(entryId); }
   public static synchronized void removeSectionIfEmpty(String chapterId,String sectionId){ Chapter c=CHAPTERS.get(chapterId); if(c!=null&&c.entries.values().stream().noneMatch(e->sectionId.equals(e.section())))c.sections.remove(sectionId); }
   public static List<Chapter> chapters(){ List<Chapter> sorted=new ArrayList<>(CHAPTERS.values()); sorted.sort(Comparator.comparingInt(Chapter::sortPriority).thenComparingLong(c->c.registrationIndex)); return sorted; }
   public static Optional<Chapter> chapter(String id){ return Optional.ofNullable(CHAPTERS.get(id)); }
   public static State state(ServerPlayer player,String chapterId,Entry entry){ return switch(displayState(player,chapterId,entry)){ case MASTERED -> State.MASTERED; case HEARD,TASTED -> State.HEARD; default -> State.UNKNOWN; }; }
   private static boolean isAlwaysVisible(Entry entry){ return "overview".equals(entry.id()) || entry.id().startsWith("guide_"); }
   public static KnowledgeDisplayState displayState(ServerPlayer player,String chapterId,Entry entry){ if(isAlwaysVisible(entry)) return KnowledgeDisplayState.MASTERED; String key=key(chapterId,entry.id()); KnowledgeFlags f=flags(player,key); if(isMastered(player,key,f)) return KnowledgeDisplayState.MASTERED; if(f.tasted()) return KnowledgeDisplayState.TASTED; if(f.heard()) return KnowledgeDisplayState.HEARD; if(f.encountered()) return KnowledgeDisplayState.ENCOUNTERED; return KnowledgeDisplayState.UNKNOWN; }
   public static boolean isMastered(ServerPlayer player,String key,KnowledgeFlags f){ return f.encountered()&&(f.heard()||f.tasted())&&(f.practiced()||coreFallback(player,key,f)); }
   private static boolean coreFallback(ServerPlayer player,String key,KnowledgeFlags f){ if(!(f.heard()&&f.tasted()&&f.eatCount()>=5)) return false; Set<String> exp=experiencedEffects(player).getOrDefault(key,Set.of()); return !exp.isEmpty() || allEffectsLowChance(key); }
   private static boolean allEffectsLowChance(String key){ if(!key.startsWith("herbal/")) return true; String path=key.substring(7); for(Map.Entry<Item,HerbDefinition> e:HerbcraftAPI.getHerbRegistry().entrySet()){ Identifier id=BuiltInRegistries.ITEM.getKey(e.getKey()); if(id!=null&&id.getPath().equals(path)) return e.getValue().effects().stream().allMatch(x->x.chance()<=0.10F); } return true; }
   public static boolean hear(ServerPlayer player,String chapterId,String entryId){ return markHeard(player,key(chapterId,entryId)); }
   public static Optional<Entry> hearRandomUnknown(ServerPlayer player,String chapterId){ Chapter c=CHAPTERS.get(chapterId); if(c==null)return Optional.empty(); List<Entry> u=new ArrayList<>(); for(Entry e:c.entries.values()) if(displayState(player,chapterId,e)==KnowledgeDisplayState.UNKNOWN) u.add(e); if(u.isEmpty())return Optional.empty(); Entry p=u.get(player.getRandom().nextInt(u.size())); markHeard(player,key(chapterId,p.id())); return Optional.of(p); }
   public static boolean markEncountered(ServerPlayer p,String key){ boolean r=flags(p,key).markEncountered(); mirror(p,key); return r; }
   public static boolean markHeard(ServerPlayer p,String key){ boolean r=flags(p,key).markHeard(); mirror(p,key); return r; }
   public static boolean markTasted(ServerPlayer p,String key){ boolean r=flags(p,key).markTasted(); mirror(p,key); return r; }
   public static boolean markPracticed(ServerPlayer p,String key){ boolean r=flags(p,key).markPracticed(); mirror(p,key); return r; }
   public static void addExperiencedEffect(ServerPlayer p,String key,String effectId){ experiencedEffects(p).computeIfAbsent(key,k->new LinkedHashSet<>()).add(effectId); mirror(p,key); }
   public static PlayerClaimMark claimMark(ServerPlayer p,String key,String claim){ return claimMarks(p).getOrDefault(key,Map.of()).getOrDefault(claim,PlayerClaimMark.UNMARKED); }
   public static PlayerClaimMark cycleClaimMark(ServerPlayer p,String key,String claim){ PlayerClaimMark next=claimMark(p,key,claim).next(); Map<String,PlayerClaimMark> m=claimMarks(p).computeIfAbsent(key,k->new HashMap<>()); if(next==PlayerClaimMark.UNMARKED)m.remove(claim); else m.put(claim,next); return next; }
   public static void verifyClaim(ServerPlayer p,String key,String claim,boolean truth){ (truth?verifiedTrueClaims(p):verifiedFalseClaims(p)).computeIfAbsent(key,k->new LinkedHashSet<>()).add(claim); mirror(p,key); }
   public static int verifiedClaimCount(ServerPlayer p,String key){ return verifiedTrueClaims(p).getOrDefault(key,Set.of()).size()+verifiedFalseClaims(p).getOrDefault(key,Set.of()).size(); }
   public static KnowledgeFlags flags(ServerPlayer p,String key){ return knowledgeFlags(p).computeIfAbsent(key,k->new KnowledgeFlags()); }
   public static String key(String chapterId,String entryId){ return chapterId+"/"+entryId; }
   public static Map<String,Integer> knowledge(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$knowledge(); }
   public static Map<String,KnowledgeFlags> knowledgeFlags(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$knowledgeFlags(); }
   public static Map<String,Set<String>> experiencedEffects(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$experiencedEffects(); }
   public static Map<String,Map<String,PlayerClaimMark>> claimMarks(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$claimMarks(); }
   public static Map<String,Set<String>> verifiedTrueClaims(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$verifiedTrueClaims(); }
   public static Map<String,Set<String>> verifiedFalseClaims(ServerPlayer p){ return ((HerbcraftPlayerData)p).herbcraft$verifiedFalseClaims(); }
   private static void mirror(ServerPlayer p,String key){ KnowledgeFlags f=flags(p,key); int v=isMastered(p,key,f)?2:(f.heard()||f.tasted()?1:0); if(v>0) knowledge(p).put(key,v); }
   public static final class Chapter { private final String id; private final Component title; private final int sortPriority; private final long registrationIndex; private final Map<String,Component> sections=new LinkedHashMap<>(); private final Map<String,Entry> entries=new LinkedHashMap<>(); private Chapter(String id,Component title,int sortPriority,long registrationIndex){this.id=id;this.title=title;this.sortPriority=sortPriority;this.registrationIndex=registrationIndex;} public String id(){return id;} public Component title(){return title;} public int sortPriority(){return sortPriority;} public Map<String,Component> sections(){return sections;} public Collection<Entry> entries(){return entries.values();} public Entry entry(String entryId){return entries.get(entryId);} }
   public record Entry(String id,String section,Component title,BiFunction<ServerPlayer,State,List<Component>> lines,Predicate<ServerPlayer> mastered,Predicate<ServerPlayer> heard) {}
   public enum State { UNKNOWN, HEARD, MASTERED; }
}
