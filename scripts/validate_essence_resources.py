#!/usr/bin/env python3
import subprocess
import sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
cmd = [sys.executable, str(ROOT / "scripts/generate_essence_resources.py"), "--check"]
raise SystemExit(subprocess.call(cmd, cwd=ROOT))
