pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://jitpack.io") }

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("file:///C:/0_Dev/android/_lao/generative-ai-android-main/generativeai/m2")
            url = uri("file:///C:/0_Dev/android/_lao/generative-ai-android-main/common/m2")
        }

        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://jitpack.io") }

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "gemini"
include(":app")
include(":shizuku")
include(":tool")
include(":utils")
include(":settings")
