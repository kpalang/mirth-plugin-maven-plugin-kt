import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
}

group = "com.kaurpalang"
version = "2.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11

}

publishing {
    publications {
        create<MavenPublication>("mirth-plugin-maven-plugin") {
            artifactId = rootProject.name
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("mirth-plugin-maven-plugin")
                description.set("A maven plugin to simplify and automate NextGen Connect plugin development. ")
                url.set("https://github.com/kpalang/mirth-plugin-maven-plugin")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Kaur Palang")
                        email.set("5758525+kpalang@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://git@github.com:kpalang/mirth-plugin-maven-plugin.git")
                    developerConnection.set("scm:git:ssh://git@github.com:kpalang/mirth-plugin-maven-plugin.git")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.maven:maven-plugin-api:3.9.3")
    implementation("org.apache.maven:maven-core:3.9.3")
    implementation("com.google.auto.service:auto-service:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.9.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}