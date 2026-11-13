#!/usr/bin/env bash
# Builds one static llama-bench executable per variant in build-config.json
# with the Android NDK, and drops them into the app as lib*.so files.
set -uo pipefail
ROOT="$(pwd)"
NDK="${ANDROID_NDK_LATEST_HOME:-${ANDROID_NDK_HOME:-}}"
[ -d "$NDK" ] || { echo "Android NDK not found"; exit 1; }
echo "Using NDK: $NDK"
JNI="$ROOT/android/app/src/main/jniLibs/arm64-v8a"
ASSETS="$ROOT/android/app/src/main/assets"
mkdir -p "$JNI" "$ASSETS"
rm -f "$JNI"/libllamabench_*.so
INFO="[]"
OK=0

n=$(jq '.variants | length' build-config.json)
for i in $(seq 0 $((n-1))); do
  name=$(jq -r ".variants[$i].name" build-config.json)
  ref=$(jq -r ".variants[$i].llama_ref // \"master\"" build-config.json)
  flags=$(jq -r ".variants[$i].cmake_flags // \"\"" build-config.json)
  echo "::group::variant $name (ref $ref)"
  src="$RUNNER_TEMP/llama-$name"
  rm -rf "$src"
  status=ok
  commit=""
  if git clone -q --filter=blob:none https://github.com/ggml-org/llama.cpp "$src" \
     && git -C "$src" checkout -q "$ref"; then
    commit=$(git -C "$src" rev-parse --short HEAD)
    # shellcheck disable=SC2086
    if cmake -S "$src" -B "$src/build" -G Ninja \
         -DCMAKE_TOOLCHAIN_FILE="$NDK/build/cmake/android.toolchain.cmake" \
         -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-29 -DANDROID_STL=c++_static \
         -DCMAKE_BUILD_TYPE=Release -DBUILD_SHARED_LIBS=OFF \
         -DGGML_OPENMP=OFF -DGGML_NATIVE=OFF -DLLAMA_CURL=OFF \
         -DLLAMA_BUILD_TESTS=OFF -DLLAMA_BUILD_SERVER=OFF \
         $flags \
       && cmake --build "$src/build" --target llama-bench -j "$(nproc)"; then
      cp "$src/build/bin/llama-bench" "$JNI/libllamabench_$name.so"
      "$NDK"/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip "$JNI/libllamabench_$name.so" || true
      OK=$((OK+1))
    else
      status=build_failed
    fi
  else
    status=checkout_failed
  fi
  echo "variant $name: $status ($commit)"
  INFO=$(jq --arg n "$name" --arg r "$ref" --arg c "$commit" --arg f "$flags" --arg s "$status" \
    '. + [{name:$n, llama_ref:$r, llama_commit:$c, cmake_flags:$f, status:$s}]' <<<"$INFO")
  echo "::endgroup::"
done

echo "$INFO" | tee "$ASSETS/binaries.json"
ls -la "$JNI"
[ "$OK" -gt 0 ] || { echo "No variant built"; exit 1; }
