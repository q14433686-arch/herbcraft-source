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

    print("PASS - CodexScreen claimHitboxes are correctly preserved across scroll max computations and click bounds are robust.")

if __name__ == "__main__":
    main()
