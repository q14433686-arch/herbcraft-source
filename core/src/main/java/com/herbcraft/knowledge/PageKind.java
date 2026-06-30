package com.herbcraft.knowledge;
import com.mojang.serialization.Codec; import net.minecraft.util.StringRepresentable;
public enum PageKind implements StringRepresentable { COMMON("common"), TRADE("trade"), RARE("rare"), ERRATA("errata"); public static final Codec<PageKind> CODEC=StringRepresentable.fromEnum(PageKind::values); private final String n; PageKind(String n){this.n=n;} public String getSerializedName(){return n;} public static PageKind byName(String name){for(PageKind k:values()) if(k.n.equals(name)) return k; return COMMON;} }
