pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        flatDir {
            dirs("app/libs")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("app/libs")
        }
    }
}