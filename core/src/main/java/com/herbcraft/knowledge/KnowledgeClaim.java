package com.herbcraft.knowledge;
public record KnowledgeClaim(String entryKey, String id, String type, String textKey, boolean truth, PageKind sourceTier, boolean suspicious) {}
