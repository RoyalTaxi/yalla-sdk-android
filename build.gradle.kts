import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
}

subprojects {
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/RoyalTaxi/yalla-sdk-android")
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
    }
}
