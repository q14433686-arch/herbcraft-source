package com.herbcraft.pharmacology;

import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.nature.NatureProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class PlayerPharmacologyState {
   private int qiBalance;
   private long lastQiRegressionAt;
   private long toxicProtectionExpiresAt;
   private long astringentHealExpiresAt;
   private final List<PlayerPharmacologyState.RecentUse> recentUses = new ArrayList<>();
   private final Map<String, PlayerPharmacologyState.PrincipleDiscovery> discoveries = new LinkedHashMap<>();

   public int qiBalance() {
      return this.qiBalance;
   }

   public void setQiBalance(int value) {
      this.qiBalance = clamp(value, -100, 100);
   }

   public long toxicProtectionExpiresAt() {
      return this.toxicProtectionExpiresAt;
   }

   public void setToxicProtectionExpiresAt(long value) {
      this.toxicProtectionExpiresAt = value;
   }

   public boolean consumeAstringentHealBonus(long gameTime, int cooldownTicks) {
      if (gameTime < this.astringentHealExpiresAt) {
         return false;
      }
      this.astringentHealExpiresAt = gameTime + cooldownTicks;
      return true;
   }

   public List<PlayerPharmacologyState.RecentUse> recentUses() {
      return this.recentUses;
   }

   public void regressQi(long gameTime, PharmacologyRules rules) {
      if (this.lastQiRegressionAt <= 0L) {
         this.lastQiRegressionAt = gameTime;
         return;
      }
      while (gameTime - this.lastQiRegressionAt >= rules.regressionIntervalTicks && this.qiBalance != 0) {
         if (this.qiBalance > 0) {
            this.qiBalance = Math.max(0, this.qiBalance - rules.regressionAmount);
         } else {
            this.qiBalance = Math.min(0, this.qiBalance + rules.regressionAmount);
         }
         this.lastQiRegressionAt += rules.regressionIntervalTicks;
      }
      if (this.qiBalance == 0) {
         this.lastQiRegressionAt = gameTime;
      }
   }

   public void cleanup(long gameTime, long nowMillis, int windowTicks) {
      this.recentUses.removeIf(use -> gameTime - use.gameTime() > windowTicks || nowMillis - use.realTimeMillis() > windowTicks * 50L);
   }

   public void addUse(String itemId, NatureProfile profile, long gameTime, long nowMillis) {
      this.recentUses.add(new RecentUse(itemId, profile, gameTime, nowMillis));
      while (this.recentUses.size() > 8) {
         this.recentUses.removeFirst();
      }
   }

   public Optional<PlayerPharmacologyState.RecentUse> latest() {
      return this.recentUses.isEmpty() ? Optional.empty() : Optional.of(this.recentUses.getLast());
   }

   public Optional<PlayerPharmacologyState.PrincipleDiscovery> discovery(String key) {
      return Optional.ofNullable(this.discoveries.get(key));
   }

   public Map<String, PlayerPharmacologyState.PrincipleDiscovery> discoveries() {
      return Map.copyOf(this.discoveries);
   }

   public void recordDiscovery(String key, String currentItemId, String previousItemId, long gameTime) {
      if (key == null || key.isBlank() || currentItemId == null || currentItemId.isBlank()) {
         return;
      }
      PlayerPharmacologyState.PrincipleDiscovery old = this.discoveries.get(key);
      int count = old == null ? 1 : old.count() + 1;
      this.discoveries.put(key, new PrincipleDiscovery(key, currentItemId, previousItemId == null ? "" : previousItemId, count, gameTime));
   }

   public void clear() {
      this.qiBalance = 0;
      this.lastQiRegressionAt = 0L;
      this.toxicProtectionExpiresAt = 0L;
      this.astringentHealExpiresAt = 0L;
      this.recentUses.clear();
      this.discoveries.clear();
   }

   public String encode() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.qiBalance).append(',').append(this.lastQiRegressionAt).append(',').append(this.toxicProtectionExpiresAt).append(',').append(this.astringentHealExpiresAt).append('#');
      boolean first = true;
      for (RecentUse use : this.recentUses) {
         if (!first) {
            builder.append(';');
         }
         builder.append(use.itemId()).append('|').append(use.gameTime()).append('|').append(use.realTimeMillis());
         first = false;
      }
      builder.append('#');
      first = true;
      for (PrincipleDiscovery discovery : this.discoveries.values()) {
         if (!first) {
            builder.append(';');
         }
         builder.append(discovery.key()).append('|')
            .append(discovery.currentItemId()).append('|')
            .append(discovery.previousItemId()).append('|')
            .append(discovery.count()).append('|')
            .append(discovery.gameTime());
         first = false;
      }
      return builder.toString();
   }

   public void decode(String value) {
      this.clear();
      if (value == null || value.isBlank()) {
         return;
      }
      String[] parts = value.split("#", 3);
      String[] header = parts[0].split(",");
      try {
         if (header.length > 0) this.qiBalance = Integer.parseInt(header[0]);
         if (header.length > 1) this.lastQiRegressionAt = Long.parseLong(header[1]);
         if (header.length > 2) this.toxicProtectionExpiresAt = Long.parseLong(header[2]);
         if (header.length > 3) this.astringentHealExpiresAt = Long.parseLong(header[3]);
      } catch (NumberFormatException ignored) {
      }
      if (parts.length >= 2 && !parts[1].isBlank()) {
         for (String entry : parts[1].split(";")) {
            String[] fields = entry.split("\\|");
            if (fields.length < 3) {
               continue;
            }
            try {
               Identifier id = Identifier.parse(fields[0]);
               NatureProfile profile = HerbNatureAPI.getNatureOrFallback(id);
               this.recentUses.add(new RecentUse(fields[0], profile, Long.parseLong(fields[1]), Long.parseLong(fields[2])));
            } catch (Exception ignored) {
            }
         }
      }
      if (parts.length >= 3 && !parts[2].isBlank()) {
         for (String entry : parts[2].split(";")) {
            String[] fields = entry.split("\\|", -1);
            if (fields.length < 5) {
               continue;
            }
            try {
               String key = fields[0];
               String currentItemId = fields[1];
               String previousItemId = fields[2];
               int count = Integer.parseInt(fields[3]);
               long gameTime = Long.parseLong(fields[4]);
               if (!key.isBlank() && !currentItemId.isBlank()) {
                  this.discoveries.put(key, new PrincipleDiscovery(key, currentItemId, previousItemId, Math.max(1, count), gameTime));
               }
            } catch (Exception ignored) {
            }
         }
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   public record RecentUse(String itemId, NatureProfile profile, long gameTime, long realTimeMillis) {
   }

   public record PrincipleDiscovery(String key, String currentItemId, String previousItemId, int count, long gameTime) {
   }
}
