# Homeostatic Temperature Compatibility Notes

Status: **P1 research note**  
Date: 2026-06-15  
Homeostatic source checked: `reference_cache/Homeostatic_26_1`, branch `26.1`

## Summary

Herbcraft should **not** implement Homeostatic temperature effects by guessing or by writing arbitrary values into Homeostatic temperature storage yet.

The `26.1` Homeostatic source shows that temperature is a simulated system based on environment, skin temperature, core temperature, wetness, insulation, water, and periodic server ticks. Unlike the already-used `environment/drinkable/*.json` thirst compatibility surface, there is no obvious item-consumption temperature JSON format comparable to `DrinkableItemManager`.

Therefore the safe conclusion for P1 is:

```text
Research complete for this round.
No temperature gameplay integration should be added until a soft-dependency adapter and balance model are designed and tested.
```

## Files inspected

Important Homeostatic temperature files:

```text
Common/src/main/java/homeostatic/common/temperature/BodyTemperature.java
Common/src/main/java/homeostatic/common/temperature/TemperatureThreshold.java
Common/src/main/java/homeostatic/common/temperature/TemperatureRange.java
Common/src/main/java/homeostatic/common/temperature/TemperatureDirection.java
Common/src/main/java/homeostatic/common/temperature/EnvironmentData.java
Common/src/main/java/homeostatic/network/ITemperature.java
Common/src/main/java/homeostatic/network/Temperature.java
Common/src/main/java/homeostatic/util/TempHelper.java
Common/src/main/java/homeostatic/event/PlayerEventHandler.java
Fabric/src/main/java/homeostatic/common/components/ComponentTemperatureData.java
Fabric/src/main/java/homeostatic/common/components/HomeostaticCardinalComponents.java
Fabric/src/main/java/homeostatic/platform/FabricPlatform.java
```

## Key findings

### 1. Temperature storage exists, but is not an item data format

Homeostatic exposes an internal temperature capability/component shape:

```java
ITemperature.setSkinTemperature(float)
ITemperature.setLastSkinTemperature(float)
ITemperature.setCoreTemperature(float)
ITemperature.setLocalTemperature(float)
ITemperature.setRelativeHumidity(double)
ITemperature.setTemperatureData(EnvironmentData, BodyTemperature)
```

Fabric stores this through Cardinal Components:

```text
HomeostaticCardinalComponents.TEMPERATURE_DATA
```

Platform access exists inside Homeostatic:

```java
Services.PLATFORM.getTemperatureData(player)
Services.PLATFORM.syncTemperatureData(serverPlayer, environmentData, bodyTemperature)
```

But this is Homeostatic's own platform service layer, not a documented Herbcraft-facing API.

### 2. Temperature is recalculated periodically

`PlayerEventHandler.onPlayerTickEvent` recalculates environment/body temperature:

```text
Every 16 or 60 ticks:
  EnvironmentData is recalculated.
  BodyTemperature is calculated.
  data.setTemperatureData(environmentData, bodyTemperature) is called.
  temperature data is synced.
```

That means a naive Herbcraft write such as "set skin temperature lower after eating ice" could be overwritten or interact badly with Homeostatic's next scheduled simulation tick.

### 3. Core temperature is dangerous to manipulate directly

Homeostatic checks core/skin thresholds:

```text
LOW      = 1.554216868
NORMAL   = 1.634457832
HIGH     = 1.799397591
SCALDING = 2.557228916
```

`Temperature.checkTemperatureLevel` can:

- increase frozen ticks when core temperature is below LOW and the player lacks Frost Resistance;
- deal hyperthermia damage when core temperature is above HIGH;
- deal scalding damage when skin temperature is above SCALDING.

Herbcraft should not directly push core/skin values across these thresholds as a casual food effect.

### 4. No drinkable-like temperature JSON was found

For thirst, Homeostatic uses:

```text
data/<namespace>/environment/drinkable/*.json
DrinkableItemManager
DrinkableItem
WaterHelper.drink(...)
```

For temperature, this round did not find an equivalent item-consumption data path such as:

```text
environment/temperature_item/*.json
environment/edible_temperature/*.json
```

Homeostatic temperature data generation focuses on biome/category/block radiation/environment simulation, not per-food temperature effects.

## Safe integration options

### Option A: Do nothing except keep drinkable compatibility

Safest for `1.1.1`/release-candidate stability.

### Option B: Add Herbcraft-side tooltip/Codex hints only

Example:

```text
寒性/冰性：may help in hot climates when a temperature compat addon is installed.
温性/热性：may help in cold climates when a temperature compat addon is installed.
```

No gameplay effect. Low risk.

### Option C: Separate optional compatibility addon

Create a future optional module/addon such as:

```text
herbcraft-homeostatic-compat
```

It can depend on Homeostatic and use its classes directly instead of reflection. This avoids forcing Homeostatic onto Herbcraft core users.

### Option D: Soft-dependency reflection adapter inside Core

Possible, but not preferred for the first implementation because:

- Homeostatic internals may change;
- reflection failures are harder to diagnose;
- temperature data is periodically recalculated;
- sync behavior must be correct.

### Option E: Mixin Homeostatic temperature simulation

Highest risk. Avoid unless Homeostatic provides no API and a separate compat addon is explicitly desired.

## Recommended balance model if implemented later

If a later addon does implement food temperature effects, prefer temporary modifiers instead of direct permanent core/skin changes.

Recommended shape:

```json
{
  "item": "minecraft:blue_ice",
  "skin_delta": -0.005,
  "core_delta": 0.0,
  "duration_ticks": 200,
  "chance": 0.35,
  "mode": "temporary_modifier"
}
```

Rules:

```text
Do not use large direct core_temperature changes.
Do not push below LOW or above HIGH intentionally.
Prefer skin/local temporary modifiers over permanent core changes.
Keep duration short.
Make effects probabilistic or very small.
Never copy the earlier negative-water mistake into temperature storage.
```

## Recommendation

For the P1/P2/P3/P4 roadmap:

```text
P1: researched this round; no temperature gameplay integration yet.
P2: low-risk external Herbcraft JSON merge layer is now implemented and server-smoked.
P3: startup-only loaded-mod/addon edible extension is now implemented and server-smoked.
P4: defer public Java API until P2/P3 receive client sanity testing and the API contract is designed.
```
