package com.herbcraft.api;
import com.herbcraft.advancement.HerbcraftAdvancements; import java.util.List; import net.minecraft.ChatFormatting; import net.minecraft.network.chat.Component; import net.minecraft.resources.ResourceKey; import net.minecraft.server.level.ServerPlayer; import net.minecraft.world.item.crafting.Recipe;
public final class KnowledgeManager { private KnowledgeManager(){} public static void awardRecipes(ServerPlayer p,List<ResourceKey<Recipe<?>>> keys){ if(!keys.isEmpty()) p.awardRecipesByKey(keys); }
 public static boolean unlock(ServerPlayer p,String ch,String e,KnowledgeAPI.State target){ return switch(target){ case UNKNOWN -> false; case HEARD -> markHeard(p,ch,e,true); case MASTERED -> markPracticed(p,ch,e,true); }; }
 public static boolean markEncountered(ServerPlayer p,String ch,String e){ return KnowledgeAPI.markEncountered(p,KnowledgeAPI.key(ch,e)); }
 public static boolean markHeard(ServerPlayer p,String ch,String e,boolean msg){ boolean r=KnowledgeAPI.markHeard(p,KnowledgeAPI.key(ch,e)); if(r){HerbcraftAdvancements.grant(p,"first_page"); if(msg)message(p,ch,e,"message.herbcraft.knowledge.heard");} return r; }
 public static boolean markTasted(ServerPlayer p,String ch,String e){ boolean r=KnowledgeAPI.markTasted(p,KnowledgeAPI.key(ch,e)); if(r)HerbcraftAdvancements.grant(p,"first_bite"); return r; }
 public static boolean markPracticed(ServerPlayer p,String ch,String e,boolean msg){ boolean r=KnowledgeAPI.markPracticed(p,KnowledgeAPI.key(ch,e)); if(r){HerbcraftAdvancements.grant(p,"master_first_entry"); if(msg)message(p,ch,e,"message.herbcraft.knowledge.mastered");} return r; }
 public static void addExperiencedEffect(ServerPlayer p,String ch,String e,String effect){ KnowledgeAPI.addExperiencedEffect(p,KnowledgeAPI.key(ch,e),effect); }
 private static void message(ServerPlayer p,String ch,String e,String key){ KnowledgeAPI.chapter(ch).map(c->c.entry(e)).ifPresent(en->p.sendOverlayMessage(Component.translatable(key,en.title().copy().withStyle(ChatFormatting.GREEN)))); }
}
