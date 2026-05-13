import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")
        classpath("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        classpath("aws.sdk.kotlin:s3:1.6.26")
    }
}

plugins {
    kotlin("jvm") version "2.3.10"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0-beta4"
    id("com.google.protobuf") version "0.9.5"
}

group = "space.chunks"
version = "2026.20.4"

val pluginName = project.property("plugin.name").toString()

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
    implementation("io.grpc:grpc-kotlin-stub:1.5.0")
    implementation("io.grpc:grpc-protobuf:1.61.0")
    implementation("io.grpc:grpc-netty:1.61.0")
    implementation("aws.sdk.kotlin:s3:1.6.26")
    api("com.google.protobuf:protobuf-kotlin:3.25.8")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
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
    // ai told me to do this, because of class loader problems with paper
    relocate(
        "com.google.protobuf",
        "space.chunks.shadow.protobuf"
    )
    archiveFileName.set("${project.name}.jar")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            "version" to project.version,
            "name" to pluginName,
        )
    }
}

tasks.register("uploadToS3") {
    dependsOn("shadowJar")
    group = "release"
    description = "uploads the fat jar to s3"

    doLast {
        val jarFile = tasks.named("shadowJar", ShadowJar::class).get().archiveFile.get().asFile

        runBlocking {
            val s3 = S3Client.fromEnvironment()
            val bucketName = System.getenv("BUCKET_NAME") ?: error("BUCKET_NAME env var not set")
            val key = "plugins/paper/$pluginName-${project.version}"

            s3.putObject {
                bucket = bucketName
                this.key = key
                body = ByteStream.fromFile(jarFile)
                contentLength = jarFile.length()
            }

            println("uploaded ${jarFile.name} to s3://$bucketName/$key")
        }
    }
}

tasks {
    runServer {
        downloadPlugins {
            modrinth("ViaVersion", "5.9.1")
        }
        minecraftVersion("1.21.11")
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
        artifact = "com.google.protobuf:protoc:3.25.8"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}
