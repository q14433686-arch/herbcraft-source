package com.herbcraft.api;
import java.util.*;
public final class KnowledgePracticeRegistry {
 private static final Map<String, Set<String>> ROUTES = new HashMap<>(); private KnowledgePracticeRegistry(){}
 public static synchronized void registerRoute(String entryKey, String route){ if(entryKey!=null&&route!=null) ROUTES.computeIfAbsent(entryKey,k->new LinkedHashSet<>()).add(route); }
 public static synchronized boolean hasRoutes(String entryKey){ return ROUTES.containsKey(entryKey)&&!ROUTES.get(entryKey).isEmpty(); }
 public static synchronized Set<String> routes(String entryKey){ return Set.copyOf(ROUTES.getOrDefault(entryKey, Set.of())); }
}
