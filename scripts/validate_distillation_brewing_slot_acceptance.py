from pathlib import Path

ingredient_path = Path('alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandIngredientSlotMixin.java')
potion_path = Path('alchemy/src/main/java/com/herbcraft/alchemy/mixin/BrewingStandPotionSlotMixin.java')
mixins_path = Path('alchemy/src/main/resources/herbcraft_alchemy.mixins.json')

ingredient_text = ingredient_path.read_text(encoding='utf-8')
potion_text = potion_path.read_text(encoding='utf-8')
mixins_text = mixins_path.read_text(encoding='utf-8')

checks = [
    ('ingredient slot imports DistilledEssenceBrewing', 'import com.herbcraft.alchemy.distillation.DistilledEssenceBrewing;' in ingredient_text),
    ('ingredient slot still supports hidden cuisine catalyst', 'HiddenCuisineCatalystBrewing.isCatalystItem(stack)' in ingredient_text),
    ('ingredient slot supports distilled essence catalysts', 'DistilledEssenceBrewing.isCatalystItem(stack)' in ingredient_text),
    ('ingredient mayPlace injection remains cancellable', '@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)' in ingredient_text),
    ('potion slot mixin source exists', potion_path.exists()),
    ('potion slot mixin imports DistilledEssenceBrewing', 'import com.herbcraft.alchemy.distillation.DistilledEssenceBrewing;' in potion_text),
    ('potion slot mayPlace accepts distillation inputs', 'DistilledEssenceBrewing.isDistillationInput(stack)' in potion_text),
    ('potion slot has no static mayPlaceItem injector (removed in 26.3; hooking it fails mixin apply)', '"mayPlaceItem"' not in potion_text),
    ('potion slot mixin registered', '"BrewingStandPotionSlotMixin"' in mixins_text),
]

failed = False
for label, ok in checks:
    if ok:
        print(f'PASS - {label}')
    else:
        failed = True
        print(f'FAIL - {label}')

if failed:
    raise SystemExit(1)

print('Distillation brewing slot acceptance validation passed.')
