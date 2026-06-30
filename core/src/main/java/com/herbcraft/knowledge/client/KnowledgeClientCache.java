package com.herbcraft.knowledge.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KnowledgeClientCache {
   private static final Map<String, Integer> STATES = new ConcurrentHashMap<>();

   private KnowledgeClientCache() {
   }

   public static void update(Map<String, Integer> states) {
      STATES.clear();
      STATES.putAll(states);
   }

   public static int state(String chapterId, String entryId) {
      return STATES.getOrDefault(chapterId + "/" + entryId, 0);
   }
}
