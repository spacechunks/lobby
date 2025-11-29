import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "2.1.10"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0-beta4"
    id("com.google.protobuf") version "0.9.5"
}

group = "space.chunks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    api("com.google.protobuf:protobuf-kotlin:3.23.1")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.named("shadowJar", ShadowJar::class) {
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

tasks {
    runServer {
        downloadPlugins {
            modrinth("ViaVersion", "5.3.1")
        }

        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
    }
}

sourceSets {
    main {
        kotlin {
            srcDirs(
                "build/generated/source/proto/main/kotlin",
            )
        }
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.23.1"
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
        }
    }
}