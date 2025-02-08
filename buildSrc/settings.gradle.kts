dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from("de.c-otto:version-catalog:2025.02.08")
        }
    }
}
