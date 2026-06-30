#!/usr/bin/env python3
"""Compatibility wrapper for the current custom nutrition effect validation stage."""
import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).with_name("validate_custom_nutrition_effects_phase7.py")), run_name="__main__")
