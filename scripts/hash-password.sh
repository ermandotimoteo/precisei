#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash mvnw -q compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
java -cp "target/classes:$(cat target/classpath.txt)" com.precisei.tools.SenhaTool "$@"
