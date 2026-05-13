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
