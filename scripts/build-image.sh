#!/usr/bin/env bash
set -euo pipefail
image="${1:?Informe a tag local ou de registro para o candidato}"
base="${JAVA_RUNTIME_REFERENCE:-eclipse-temurin:17-jre-jammy}"
test -f target/precisei-0.0.1-SNAPSHOT.jar
docker pull "$base"
resolved="$(docker image inspect --format '{{index .RepoDigests 0}}' "$base")"
[[ "$resolved" =~ @sha256:[a-f0-9]{64}$ ]] || { echo 'Imagem base sem digest verificável.' >&2; exit 1; }
mkdir -p artifacts
printf '%s\n' "$resolved" > artifacts/java-runtime.txt
docker build --build-arg "JAVA_RUNTIME=$resolved" --label "org.opencontainers.image.revision=$(git rev-parse HEAD)" -t "$image" .
