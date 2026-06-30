package com.herbcraft.api;
public enum PlayerClaimMark { UNMARKED, THINK_TRUE, THINK_FALSE; public PlayerClaimMark next(){return switch(this){case UNMARKED -> THINK_TRUE; case THINK_TRUE -> THINK_FALSE; case THINK_FALSE -> UNMARKED;};} public String symbol(){return switch(this){case UNMARKED -> "○"; case THINK_TRUE -> "✓"; case THINK_FALSE -> "✕";};} }
