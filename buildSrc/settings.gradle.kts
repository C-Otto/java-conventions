dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from("de.c-otto:version-catalog:2024.06.07")
        }
    }
}
