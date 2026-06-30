# Example Herbcraft Read-only/Event API Addon

This is a documentation-only P4.4 example for code addon authors.

It demonstrates:

```text
com.herbcraft.api.HerbcraftDataAPI
com.herbcraft.api.HerbcraftEvents
```

The example follows the current P4 boundary:

```text
JSON provides content.
Java reads current Herbcraft data and observes events.
Java does not register/replace Herbcraft content in this example.
```

## Files

```text
fabric.mod.json
src/main/java/com/example/herbcraftapi/ExampleHerbcraftApiAddon.java
```

## Demonstrated reads

```java
HerbcraftDataAPI.nature(itemId)
HerbcraftDataAPI.ingredientEntryId(itemId)
```

## Demonstrated events

```java
HerbcraftEvents.NUTRITION_APPLIED
HerbcraftEvents.INGREDIENT_DISCOVERED
HerbcraftEvents.FOOD_NATURE_RESOLVED
HerbcraftEvents.EXTERNAL_DATA_RELOADED
```

## Dependency note

The example uses:

```json
"herbcraft": ">=1.1.3"
```

because P4 APIs are part of the `1.1.3` planning line, even though the current working source may still build test jars with the `1.1.2` version string until the release bump is chosen.
