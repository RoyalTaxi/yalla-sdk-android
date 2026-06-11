enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "YallaSdkGitHubPackages"
            url = uri("https://maven.pkg.github.com/RoyalTaxi/yalla-sdk")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user"))
                    .orNull
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.key"))
                    .orNull
            }
        }
    }
}

rootProject.name = "yalla-sdk-android"

val yallaSdkDir = file("../yalla-sdk")
val useLocalYallaSdk = providers.gradleProperty("useLocalYallaSdk")
    .map(String::toBoolean)
    .getOrElse(true)

if (useLocalYallaSdk && gradle.parent == null && yallaSdkDir.isDirectory) {
    includeBuild(yallaSdkDir) {
        dependencySubstitution {
            substitute(module("uz.yalla.sdk:components"))
                .using(project(":components"))
            substitute(module("uz.yalla.sdk:core"))
                .using(project(":core"))
            substitute(module("uz.yalla.sdk:design"))
                .using(project(":design"))
            substitute(module("uz.yalla.sdk:maps"))
                .using(project(":maps"))
            substitute(module("uz.yalla.sdk:media"))
                .using(project(":media"))
            substitute(module("uz.yalla.sdk:resources"))
                .using(project(":resources"))
            substitute(module("uz.yalla.sdk:telemetry"))
                .using(project(":telemetry"))
        }
    }
}

include(":design")
include(":resources")
include(":components")
include(":bridges")
include(":maps")
