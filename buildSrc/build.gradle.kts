import java.util.Optional

val catalogs = project.extensions.getByType(VersionCatalogsExtension::class)
val catalog = catalogs.named("libs")

val rootDir = project.rootDir.toString()
File("$rootDir/../gradle/").mkdir()
File("$rootDir/../gradle/libs.versions.toml").writeText(getToml())

File("$rootDir/../src/main/resources/").mkdir()
File("$rootDir/../src/main/resources/versions.properties").writeText(getProperties())


fun getToml(): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("[versions]\n")
    catalog.versionAliases.forEach {
        stringBuilder.append(this.getTomlVersionEntry(it, catalog.findVersion(it)))
    }

    stringBuilder.append("[libraries]\n")
    catalog.libraryAliases.forEach {
        stringBuilder.append(this.getTomlLibraryEntry(it, catalog.findLibrary(it)))
    }

    return stringBuilder.toString()
}

fun getTomlVersionEntry(name: String, value: Optional<VersionConstraint>): String {
    val valueForTomlFile = "\"" + value.orElseThrow().requiredVersion + "\""
    return getTomlEntry(name, valueForTomlFile)
}

fun getTomlLibraryEntry(name: String, value: Optional<Provider<MinimalExternalModuleDependency>>): String {
    val data = value.orElseThrow().get()
    return getTomlEntry(name, "{group = \"${data.group}\", name = \"${data.name}\", version = \"${data.version}\"}")
}

fun getTomlEntry(name: String, value: String): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append(name.replace(".", "-"))
    stringBuilder.append(" = ")
    stringBuilder.append(value)
    stringBuilder.append("\n")
    return stringBuilder.toString()
}

fun getProperties(): String {
    val stringBuilder = StringBuilder()
    catalog.versionAliases.forEach {
        val version = catalog.findVersion(it).orElseThrow().requiredVersion
        stringBuilder.append("${it}=${version}\n")
    }

    return stringBuilder.toString()
}