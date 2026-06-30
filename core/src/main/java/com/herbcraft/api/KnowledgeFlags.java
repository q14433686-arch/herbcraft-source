package com.herbcraft.api;

public final class KnowledgeFlags {
   private boolean encountered;
   private boolean heard;
   private boolean tasted;
   private boolean practiced;
   private int eatCount;

   public boolean encountered() { return encountered; }
   public boolean heard() { return heard; }
   public boolean tasted() { return tasted; }
   public boolean practiced() { return practiced; }
   public int eatCount() { return eatCount; }
   public boolean markEncountered() { boolean old = encountered; encountered = true; return !old; }
   public boolean markHeard() { boolean old = heard; encountered = true; heard = true; return !old; }
   public boolean markTasted() { boolean old = tasted; encountered = true; tasted = true; eatCount++; return !old; }
   public boolean markPracticed() { boolean old = practiced; encountered = true; practiced = true; return !old; }
   public void setLegacy(int state) {
      if (state >= 1) { encountered = true; heard = true; }
      if (state >= 2) { encountered = true; heard = true; tasted = true; practiced = true; eatCount = Math.max(eatCount, 1); }
   }
   public String encode() { return (encountered ? 1 : 0) + "," + (heard ? 1 : 0) + "," + (tasted ? 1 : 0) + "," + (practiced ? 1 : 0) + "," + eatCount; }
   public static KnowledgeFlags decode(String value) {
      KnowledgeFlags flags = new KnowledgeFlags();
      if (value == null || value.isBlank()) return flags;
      String[] p = value.split(",");
      try {
         if (p.length > 0) flags.encountered = "1".equals(p[0]) || Boolean.parseBoolean(p[0]);
         if (p.length > 1) flags.heard = "1".equals(p[1]) || Boolean.parseBoolean(p[1]);
         if (p.length > 2) flags.tasted = "1".equals(p[2]) || Boolean.parseBoolean(p[2]);
         if (p.length > 3) flags.practiced = "1".equals(p[3]) || Boolean.parseBoolean(p[3]);
         if (p.length > 4) flags.eatCount = Integer.parseInt(p[4]);
      } catch (Exception ignored) {}
      return flags;
   }
}
