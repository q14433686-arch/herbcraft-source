# Author Decisions Record — UI Data-Driven + Hardcore Mod

Date: 2026-06-17  
Status: **confirmed by author**

## Confirmed decisions

### 1. UI Data-Driven Conversion

- **Layer 1 (theme config JSON) + Layer 2 (component registry): confirmed for 1.2.0**
- Layer 3 (declarative layout engine): deferred to future version
- All UI constants (colors, sizes, paddings, row heights) will be extracted to `codex_theme.json` and `nutrition_theme.json`
- Resource packs will be able to override these files

### 2. UI Textures / Art

- Author confirms they cannot produce art themselves
- All textures will be AI-generated, with author review and multi-round discussion
- Art direction will be finalized through iterative prompt refinement before generation
- Author has final approval on all generated textures

### 3. Hardcore Mod

- **Confirmed: Mode B — independent hardcore survival mod + Herbcraft compat bridge**
- The mod is NOT a Herbcraft addon; it is a standalone core mod
- Name candidates: `严生` or `难存` (author to decide final)
- It will own: temperature, hydration, health slider, medical items, seasonal herb mechanics
- Herbcraft bridge: reads `HerbcraftDataAPI.nature(item)` to affect temperature/health
- Medical system will require new items (aloe, plantain, etc.) registered by this mod

### 4. Homeostatic

- Author does not prefer Homeostatic's implementation style
- But will reference it to reduce development cost where practical
- Implementation will be original, not a fork

### 5. cocoa_fat

- Confirmed as placeholder only; removed from all planning as a real feature

### 6. Implementation Order

```
Step 1: UI data-driven Layer 1+2 (theme JSON + component registry)
Step 2: UI beautification (textures, layout, icons, breadcrumb, scrollbar, search)
Step 3: UI extension support (dynamic dimensions, addon theme entries)
Step 4: Hardcore mod skeleton (独立项目)
```

百草可食 1.2.0 covers Steps 1-3.  
Hardcore mod is a separate project after 1.2.0 UI work is stable.
