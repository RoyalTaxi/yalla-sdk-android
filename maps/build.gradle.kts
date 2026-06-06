import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = "uz.yalla.sdk.android"
version = "0.1.0-SNAPSHOT"

android {
    namespace = "uz.yalla.sdk.android.maps"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(libs.yalla.sdk.maps)
    api(libs.yalla.sdk.core)
    api(libs.yalla.sdk.design)
    api(libs.yalla.sdk.resources)

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.google.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.maplibre.compose)
    implementation(libs.geo)
    implementation(libs.geo.compose)

    implementation(libs.koin.core)
    implementation(libs.koin.compose)

    implementation(libs.kotlinx.coroutines.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "uz.yalla.sdk"
                artifactId = "yalla-maps-android"
                version = project.version.toString()
            }
        }
    }
}
