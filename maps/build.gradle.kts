import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "uz.yalla.sdk.android"
version = "0.2.8"

android {
    namespace = "uz.yalla.sdk.android.maps"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)

    implementation(libs.play.services.maps)
    implementation(libs.maplibre.android.sdk)
    implementation(libs.maplibre.android.plugin.annotation)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test)
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
