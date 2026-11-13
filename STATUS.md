run: 1
commit: 024c17a6911c73d811cfe90d1078dec52ed7c69c
result: success
time: 2026-11-13T15:25:28Z

== binaries.json ==
[
  {
    "name": "default",
    "llama_ref": "master",
    "llama_commit": "fe14cad51",
    "cmake_flags": "-DGGML_CPU_ARM_ARCH=armv8.2-a+dotprod+fp16",
    "status": "ok"
  },
  {
    "name": "kleidiai",
    "llama_ref": "master",
    "llama_commit": "fe14cad51",
    "cmake_flags": "-DGGML_CPU_ARM_ARCH=armv8.2-a+dotprod+fp16 -DGGML_CPU_KLEIDIAI=ON",
    "status": "ok"
  }
]

== native.log (tail) ==
[452/547] Building CXX object src/CMakeFiles/llama.dir/llama-batch.cpp.o
[453/547] Building CXX object src/CMakeFiles/llama.dir/llama-hparams.cpp.o
[454/547] Building CXX object src/CMakeFiles/llama.dir/llama-impl.cpp.o
[455/547] Building CXX object src/CMakeFiles/llama.dir/llama-io.cpp.o
[456/547] Building CXX object src/CMakeFiles/llama.dir/llama-graph.cpp.o
[457/547] Building CXX object src/CMakeFiles/llama.dir/llama-context.cpp.o
[458/547] Building CXX object src/CMakeFiles/llama.dir/llama-grammar.cpp.o
[459/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache-iswa.cpp.o
[460/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache-dsa.cpp.o
[461/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache-dsa-iswa.cpp.o
[462/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache-msa.cpp.o
[463/547] Building CXX object src/CMakeFiles/llama.dir/llama-memory.cpp.o
[464/547] Building CXX object src/CMakeFiles/llama.dir/llama-memory-hybrid.cpp.o
[465/547] Building CXX object src/CMakeFiles/llama.dir/llama-moe-cache.cpp.o
[466/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache-dsv4.cpp.o
[467/547] Building CXX object src/CMakeFiles/llama.dir/llama-memory-hybrid-iswa.cpp.o
[468/547] Building CXX object src/CMakeFiles/llama.dir/llama-kv-cache.cpp.o
[469/547] Building CXX object src/CMakeFiles/llama.dir/llama-memory-hybrid-idx.cpp.o
[470/547] Building CXX object src/CMakeFiles/llama.dir/llama-mmap.cpp.o
[471/547] Building CXX object src/CMakeFiles/llama.dir/llama-memory-recurrent.cpp.o
[472/547] Building CXX object src/CMakeFiles/llama.dir/llama-model-saver.cpp.o
[473/547] Building CXX object src/CMakeFiles/llama.dir/llama-model-loader.cpp.o
[474/547] Building CXX object src/CMakeFiles/llama.dir/llama-quant.cpp.o
[475/547] Building CXX object src/CMakeFiles/llama.dir/llama-sampler.cpp.o
[476/547] Building CXX object src/CMakeFiles/llama.dir/llama-model.cpp.o
[477/547] Building CXX object src/CMakeFiles/llama.dir/llama-vocab.cpp.o
[478/547] Building CXX object common/CMakeFiles/llama-common-base.dir/build-info.cpp.o
[479/547] Linking CXX static library common/libllama-common-base.a
[480/547] Building CXX object src/CMakeFiles/llama.dir/unicode.cpp.o
[481/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/parsers.cpp.o
[482/547] Building CXX object src/CMakeFiles/llama.dir/unicode-data.cpp.o
[483/547] Linking CXX static library src/libllama.a
[484/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/cohere2moe.cpp.o
[485/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/functionary-v3-2.cpp.o
[486/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/deepseek.cpp.o
[487/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/gigachat-v3.cpp.o
[488/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/gemma4.cpp.o
[489/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/gpt-oss.cpp.o
[490/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/harmony-xml.cpp.o
[491/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/kimi-k2.cpp.o
[492/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/k2-horizon.cpp.o
[493/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/kimi-k3.cpp.o
[494/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/lfm2.cpp.o
[495/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/ling3.cpp.o
[496/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/llm-jp-harmony.cpp.o
[497/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/minicpm5.cpp.o
[498/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/minimax-m3.cpp.o
[499/547] Building CXX object vendor/cpp-httplib/CMakeFiles/cpp-httplib.dir/httplib.cpp.o
[500/547] Linking CXX static library vendor/cpp-httplib/libcpp-httplib.a
[501/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/ministral3.cpp.o
[502/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/muse-glimmer.cpp.o
[503/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/muse-spark.cpp.o
[504/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/qwen3-coder.cpp.o
[505/547] Building CXX object common/CMakeFiles/llama-common.dir/parsers/translate-gemma.cpp.o
[506/547] Building CXX object common/CMakeFiles/llama-common.dir/chat-auto-parser-helpers.cpp.o
[507/547] Building CXX object common/CMakeFiles/llama-common.dir/chat-auto-parser-generator.cpp.o
[508/547] Building CXX object common/CMakeFiles/llama-common.dir/chat-tts-norm.cpp.o
[509/547] Building CXX object common/CMakeFiles/llama-common.dir/chat-peg-parser.cpp.o
[510/547] Building CXX object common/CMakeFiles/llama-common.dir/chat-diff-analyzer.cpp.o
[511/547] Building CXX object common/CMakeFiles/llama-common.dir/console.cpp.o
[512/547] Building CXX object common/CMakeFiles/llama-common.dir/chat.cpp.o
[513/547] Building CXX object common/CMakeFiles/llama-common.dir/debug.cpp.o
[514/547] Building CXX object common/CMakeFiles/llama-common.dir/common.cpp.o
[515/547] Building CXX object common/CMakeFiles/llama-common.dir/file-lock.cpp.o
[516/547] Building CXX object common/CMakeFiles/llama-common.dir/arg.cpp.o
[517/547] Building CXX object common/CMakeFiles/llama-common.dir/fit.cpp.o
[518/547] Building CXX object common/CMakeFiles/llama-common.dir/imatrix-loader.cpp.o
[519/547] Building CXX object common/CMakeFiles/llama-common.dir/hf-cache.cpp.o
[520/547] Building CXX object common/CMakeFiles/llama-common.dir/json-schema.cpp.o
[521/547] Building CXX object common/CMakeFiles/llama-common.dir/download.cpp.o
[522/547] Building CXX object common/CMakeFiles/llama-common.dir/llguidance.cpp.o
[523/547] Building CXX object common/CMakeFiles/llama-common.dir/log.cpp.o
[524/547] Building CXX object common/CMakeFiles/llama-common.dir/ngram-cache.cpp.o
[525/547] Building CXX object common/CMakeFiles/llama-common.dir/json-schema-to-grammar.cpp.o
[526/547] Building CXX object common/CMakeFiles/llama-common.dir/ngram-mod.cpp.o
[527/547] Building CXX object common/CMakeFiles/llama-common.dir/ngram-map.cpp.o
[528/547] Building CXX object common/CMakeFiles/llama-common.dir/json.cpp.o
[529/547] Building CXX object common/CMakeFiles/llama-common.dir/reasoning-budget.cpp.o
[530/547] Building CXX object common/CMakeFiles/llama-common.dir/sampling.cpp.o
[531/547] Building CXX object common/CMakeFiles/llama-common.dir/subproc.cpp.o
[532/547] Building CXX object common/CMakeFiles/llama-common.dir/trie.cpp.o
[533/547] Building CXX object common/CMakeFiles/llama-common.dir/preset.cpp.o
[534/547] Building CXX object common/CMakeFiles/llama-common.dir/unicode.cpp.o
[535/547] Building CXX object common/CMakeFiles/llama-common.dir/peg-parser.cpp.o
[536/547] Building CXX object common/CMakeFiles/llama-common.dir/speculative.cpp.o
[537/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/lexer.cpp.o
[538/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/string.cpp.o
[539/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/parser.cpp.o
[540/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/runtime.cpp.o
[541/547] Building CXX object tools/llama-bench/CMakeFiles/llama-bench.dir/main.cpp.o
[542/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/caps.cpp.o
[543/547] Building CXX object common/CMakeFiles/llama-common.dir/jinja/value.cpp.o
[544/547] Linking CXX static library common/libllama-common.a
[545/547] Building CXX object tools/llama-bench/CMakeFiles/llama-bench-impl.dir/llama-bench.cpp.o
[546/547] Linking CXX static library tools/llama-bench/libllama-bench-impl.a
[547/547] Linking CXX executable bin/llama-bench
variant kleidiai: ok (fe14cad51)
::endgroup::
[
  {
    "name": "default",
    "llama_ref": "master",
    "llama_commit": "fe14cad51",
    "cmake_flags": "-DGGML_CPU_ARM_ARCH=armv8.2-a+dotprod+fp16",
    "status": "ok"
  },
  {
    "name": "kleidiai",
    "llama_ref": "master",
    "llama_commit": "fe14cad51",
    "cmake_flags": "-DGGML_CPU_ARM_ARCH=armv8.2-a+dotprod+fp16 -DGGML_CPU_KLEIDIAI=ON",
    "status": "ok"
  }
]
total 10812
drwxr-xr-x 2 runner runner    4096 Nov 13 15:23 .
drwxr-xr-x 3 runner runner    4096 Nov 13 15:17 ..
-rw-r--r-- 1 runner runner       0 Nov 13 15:17 .keep
-rwxr-xr-x 1 runner runner 5471240 Nov 13 15:20 libllamabench_default.so
-rwxr-xr-x 1 runner runner 5588056 Nov 13 15:23 libllamabench_kleidiai.so

== gradle.log (tail) ==

Welcome to Gradle 8.14.3!

Here are the highlights of this release:
 - Java 24 support
 - GraalVM Native Image toolchain selection
 - Enhancements to test reporting
 - Build Authoring improvements

For more details see https://docs.gradle.org/8.14.3/release-notes.html

To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.14.3/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:buildKotlinToolingMetadata
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseResValues
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :app:generateReleaseResources
> Task :app:mergeReleaseResources
> Task :app:packageReleaseResources
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease
> Task :app:parseReleaseLocalResources

> Task :app:processReleaseMainManifest
/home/runner/work/phone-llm-bench/phone-llm-bench/android/app/src/main/AndroidManifest.xml:13:9-41 Warning:
	android:extractNativeLibs should not be specified in this source AndroidManifest.xml file. See https://d.android.com/guide/topics/manifest/application-element#extractNativeLibs for more information.
The AGP Upgrade Assistant can remove the attribute from the AndroidManifest.xml file and update the build file accordingly. See https://d.android.com/studio/build/agp-upgrade-assistant for more information.

> Task :app:processReleaseManifest
> Task :app:processReleaseManifestForPackage
> Task :app:javaPreCompileRelease
> Task :app:extractProguardFiles
> Task :app:mergeReleaseJniLibFolders
> Task :app:desugarReleaseFileDependencies
> Task :app:checkReleaseDuplicateClasses
> Task :app:mergeReleaseStartupProfile
> Task :app:mergeReleaseNativeLibs
> Task :app:processReleaseResources
> Task :app:mergeExtDexRelease
> Task :app:mergeReleaseArtProfile
> Task :app:mergeReleaseShaders
> Task :app:compileReleaseShaders NO-SOURCE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets

> Task :app:stripReleaseDebugSymbols
Unable to strip the following libraries, packaging them as they are: libllamabench_default.so, libllamabench_kleidiai.so.

> Task :app:compressReleaseAssets
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractReleaseNativeSymbolTables
> Task :app:collectReleaseDependencies
> Task :app:mergeReleaseNativeDebugMetadata NO-SOURCE
> Task :app:sdkReleaseDependencyData
> Task :app:validateSigningRelease
> Task :app:writeReleaseAppMetadata
> Task :app:writeReleaseSigningConfigVersions
> Task :app:optimizeReleaseResources
> Task :app:compileReleaseKotlin
> Task :app:compileReleaseJavaWithJavac NO-SOURCE
> Task :app:generateReleaseLintVitalReportModel
> Task :app:dexBuilderRelease
> Task :app:mergeReleaseGlobalSynthetics
> Task :app:processReleaseJavaRes
> Task :app:mergeReleaseJavaResource
> Task :app:mergeDexRelease
> Task :app:compileReleaseArtProfile
> Task :app:packageRelease
> Task :app:createReleaseApkListingFileRedirect
> Task :app:lintVitalAnalyzeRelease
> Task :app:lintVitalReportRelease
> Task :app:lintVitalRelease
> Task :app:assembleRelease
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run_3-1794583422437.json

[Incubating] Problems report is available at: file:///home/runner/work/phone-llm-bench/phone-llm-bench/android/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.14.3/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 1m 47s
48 actionable tasks: 48 executed
