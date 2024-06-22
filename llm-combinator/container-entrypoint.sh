#!/usr/bin/env bash
set -euo pipefail

# Sole purpose of this script is to allow user override JVM_ARGS.

real_jvm_args=(
  "-Xms192m"
  "-Xmx256m"
)

if [[ -n "${JVM_ARGS:-}" ]]; then
  read -r -a real_jvm_args <<< "$JVM_ARGS"
fi

echo "JVM args: ${real_jvm_args[@]} (set env var JVM_ARGS to override)"

java "${real_jvm_args[@]}" "-jar" "/app/llm-combinator-all.jar"
