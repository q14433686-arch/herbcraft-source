#!/usr/bin/env bash
set -euo pipefail
BASE="${HERBCRAFT_JDK_CACHE:-/tmp/herbcraft-jdk25}"
JDK="$BASE/jdk-25"
mkdir -p "$BASE"
if [ ! -x "$JDK/bin/java" ]; then
  cd "$BASE"
  URL=$(curl -sL -H 'User-Agent: arena-agent' 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&heap_size=normal&image_type=jdk&jvm_impl=hotspot&os=linux&vendor=eclipse' \
    | python3 -c "import sys,json; data=json.load(sys.stdin); print(data[0]['binary']['package']['link'])")
  echo "Downloading JDK 25 from: $URL"
  curl -L --fail --retry 2 -H 'User-Agent: arena-agent' -o jdk25.tar.gz "$URL"
  tar -xzf jdk25.tar.gz
  DIR=$(find . -maxdepth 1 -type d -name 'jdk-*' | head -1)
  rm -rf "$JDK"
  mv "$DIR" "$JDK"
fi
"$JDK/bin/java" -version
cat <<EOF

Use with:
JAVA_HOME=$JDK ./gradlew build --rerun-tasks
EOF
