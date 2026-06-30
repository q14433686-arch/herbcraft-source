package com.herbcraft.pharmacology;

public enum SevenEmotionType {
   NONE(null),
   XIANG_XU("message.herbcraft.pharmacology.xiang_xu"),
   XIANG_SHI("message.herbcraft.pharmacology.xiang_shi"),
   XIANG_WEI("message.herbcraft.pharmacology.xiang_wei"),
   XIANG_SHA("message.herbcraft.pharmacology.xiang_sha"),
   XIANG_E("message.herbcraft.pharmacology.xiang_e"),
   XIANG_FAN("message.herbcraft.pharmacology.xiang_fan"),
   SINGLE(null);

   private final String messageKey;

   SevenEmotionType(String messageKey) {
      this.messageKey = messageKey;
   }

   public String messageKey() {
      return this.messageKey;
   }
}
