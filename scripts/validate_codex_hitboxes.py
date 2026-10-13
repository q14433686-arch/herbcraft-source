import sys
from pathlib import Path

def main():
    screen_file = Path("core/src/main/java/com/herbcraft/knowledge/client/CodexScreen.java")
    if not screen_file.exists():
        print(f"Error: {screen_file} not found.")
        sys.exit(1)

    content = screen_file.read_text(encoding="utf-8")

    # 1. contentHeight should NOT contain this.claimHitboxes.clear()
    # Let's find contentHeight method
    lines = content.splitlines()
    content_height_start = -1
    for i, line in enumerate(lines):
        if "private int contentHeight" in line:
            content_height_start = i
            break

    if content_height_start == -1:
        print("Error: contentHeight method not found in CodexScreen.")
        sys.exit(1)

    # Check the next 20 lines inside contentHeight for claimHitboxes.clear()
    for i in range(content_height_start, min(len(lines), content_height_start + 25)):
        if "claimHitboxes.clear()" in lines[i]:
            print(f"Error: contentHeight contains claimHitboxes.clear() at line {i+1}!")
            sys.exit(1)

    # 2. Check that renderRightPanel adds hitboxes with wrappedHeight
    if "CodexTextLayout.wrappedHeight" not in content or "claimHitboxes.add" not in content:
        print("Error: renderRightPanel does not compute robust wrappedHeight for claimHitboxes.")
        sys.exit(1)

    # 3. Check that mouseClicked checks right panel bounds
    if "event.x() >= layout.rightX" not in content:
        print("Error: mouseClicked does not check right panel bounds before processing claimHitboxes.")
        sys.exit(1)

    # 4. MC 26.3 (SDL) mouse ids: left = 1. A literal `button() == 0` silently ignores every left click.
    if "event.button() == 0" in content:
        print("Error: CodexScreen compares event.button() with 0; MC 26.3 left button is InputConstants.MOUSE_BUTTON_LEFT (1).")
        sys.exit(1)
    if "event.button() == InputConstants.MOUSE_BUTTON_LEFT" not in content:
        print("Error: CodexScreen does not check InputConstants.MOUSE_BUTTON_LEFT for clicks.")
        sys.exit(1)

    # 5. Overrides must be annotated so a wrong signature fails compilation instead of silently never being called.
    for sig in ("public boolean mouseClicked(MouseButtonEvent event, boolean doubled)",
                "public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY)"):
        idx = content.find(sig)
        if idx == -1 or "@Override" not in content[max(0, idx - 80):idx]:
            print(f"Error: missing @Override on: {sig}")
            sys.exit(1)

    print("PASS - CodexScreen claimHitboxes are correctly preserved across scroll max computations, click bounds are robust, and left-click uses MOUSE_BUTTON_LEFT.")

if __name__ == "__main__":
    main()
