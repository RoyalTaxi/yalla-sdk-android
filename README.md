# yalla-sdk-android

Pure native Android SDK for Yalla.

This repository is intentionally separate from the Kotlin Multiplatform SDK and
from the Compose Multiplatform UI package. Android is a first-class native UI
consumer, not the source of truth for other platforms.

## Intended Layers

- `design-android`: Android/Compose design tokens and theme adapters.
- `resources-android`: Android drawables, fonts, and generated asset accessors.
- `components`: Android-native and Android-hosted Jetpack Compose components.

The canonical design contract should live in platform-neutral token/resource
specs, then be implemented or generated into Android and iOS native adapters.

The `components` module may reuse platform-agnostic Compose UI from
`uz.yalla.sdk:components`. In local development, `settings.gradle.kts` resolves
that dependency from `../yalla-sdk` when the sibling repository is present.

## Resources

Native Android strings are generated from
[`RoyalTaxi/yalla-resources`](https://github.com/RoyalTaxi/yalla-resources) into
`resources/src/main/res/values*/strings.xml`.

Native Android icons are generated from the same repo's canonical SVG sources
into `resources/src/main/res/drawable/ic_*.xml` VectorDrawable files.

PNG images, fonts, and JSON files are generated into
`resources/src/main/res/drawable-nodpi`, `resources/src/main/res/font`, and
`resources/src/main/res/raw`.

Do not edit generated resources by hand. Change the canonical source in
`yalla-resources`, then run:

```bash
python3 tools/yalla_resources.py sync --no-cmp --no-ios
```
