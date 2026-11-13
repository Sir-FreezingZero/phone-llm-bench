# phone-llm-bench

An automated lab for making small LLMs run faster on a **Moto G (2025)**
(MediaTek Dimensity 6300: 2× Cortex-A76 + 6× Cortex-A55, Mali-G57 MC2, 4 GB RAM).

## Pieces
- **`android/`**: the *LLM Speed Lab* app. Tap **Run tests** and it fetches `plan.json`,
  downloads the models, runs each test with a bundled `llama-bench`, and uploads
  `results/<timestamp>.json`. It runs as a foreground service, so the screen can be off.
- **`build-config.json`**: which `llama-bench` builds go inside the app (llama.cpp ref + cmake flags).
  Any change here makes GitHub Actions build a new APK and publish it as a release (`build-N`).
- **`plan.json`**: the experiments for the next run. Claude edits this between rounds.
- **`results/`**: one JSON file per run (llama-bench output, temperatures, device info, errors).
- **`ci-status`** branch: the latest build report (so build errors can be read with git).

## plan.json
- `version`: a label for this round.
- `models[]`: `name` + direct `.gguf` URL. Models not listed are deleted from the phone.
- `tests[]`: `id`, `model`, `args` (raw llama-bench args; comma lists sweep values),
  optional `binary` (a variant name from build-config.json, default `default`),
  optional `pin` (cpu list for taskset, e.g. `"6,7"` = the two A76 cores).
- `cooldown_c` / `cooldown_max_wait_s`: wait before each test until the battery is this cool.
- `test_timeout_min`: kill a test that runs longer than this.

## Setup on the phone
1. Install the APK from the latest release.
2. Create a fine-grained GitHub token limited to this repo, with **Contents: Read and write**.
3. Open the app, paste the token, tap **Save**, then **Run tests** (ideally while charging).
