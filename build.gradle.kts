plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = "de.c-otto"
version = "2023.03.22"
val pluginDescription = "This provides some defaults for Java projects"

gradlePlugin {
    plugins.create("java-conventions") {
        id = "${project.group}.java-conventions"
        implementationClass = "de.cotto.javaconventions.JavaConventionsPlugin"
        description = pluginDescription
        displayName = project.name
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    if (name.endsWith("PluginMarkerMaven")) {
        pom {
            addInformationToPom()
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(platform(libs.platform))
    implementation(libs.gradleplugins.cpd)
    implementation(libs.gradleplugins.errorprone)
    implementation(libs.gradleplugins.nullaway)
    implementation(libs.gradleplugins.pitest)
    implementation(libs.gradleplugins.spotbugs)
    implementation(libs.gradleplugins.testLogger)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc.configure {
    (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            pom {
                addInformationToPom()
            }
            repositories {
                maven {
                    name = "OSSRH"
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    credentials {
                        username = "C-Otto"
                        password = System.getenv("OSSRH_PASSWORD") ?: return@credentials
                    }
                }
            }
        }
    }
}

tasks.publish {
    dependsOn("check")
}

configure<SigningExtension> {
    isRequired = true

    val publishing: PublishingExtension by project

    useGpgCmd()
    sign(publishing.publications)
}

fun MavenPom.addInformationToPom() {
    val pluginUrl = "https://github.com/C-Otto/java-conventions"

    name.set("Java Conventions")
    description.set(pluginDescription)
    url.set(pluginUrl)
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            id.set("cotto")
            name.set("Carsten Otto")
            email.set("git@c-otto.de")
        }
    }
    scm {
        connection.set("scm:git:git://github.com/C-Otto/java-conventions.git")
        developerConnection.set("scm:git:ssh://github.com/C-Otto/java-conventions.git")
        url.set(pluginUrl)
    }
}
