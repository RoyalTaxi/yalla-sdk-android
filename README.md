# yalla-sdk-android

Pure native Android SDK for Yalla.

This repository is intentionally separate from the Kotlin Multiplatform SDK and
from the Compose Multiplatform UI package. Android is a first-class native UI
consumer, not the source of truth for other platforms.

## Intended Layers

- `design-android`: Android/Compose design tokens and theme adapters.
- `resources-android`: Android drawables, fonts, and generated asset accessors.
- `components-android`: Android-only Jetpack Compose components.
- `sdk`: current starter Android library module.

The canonical design contract should live in platform-neutral token/resource
specs, then be implemented or generated into Android and iOS native adapters.

## Resources

Native Android strings are generated from
[`RoyalTaxi/yalla-resources`](https://github.com/RoyalTaxi/yalla-resources) into
`sdk/src/main/res/values*/yalla_strings.xml`.

Do not edit generated `yalla_strings.xml` files by hand. Change the canonical
catalog in `yalla-resources`, then run:

```bash
python3 tools/yalla_resources.py sync --no-cmp --no-ios
```
