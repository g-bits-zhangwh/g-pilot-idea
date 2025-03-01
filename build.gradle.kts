import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.tasks.Copy

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.changelog") version "2.0.0"
    id("org.jetbrains.qodana") version "0.1.13"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

dependencies {
    implementation("dev.gitlive:kotlin-diff-utils:5.0.7")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1") {
        exclude("org.slf4j")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("io.github.kezhenxu94:cache-lite:0.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // test libraries
    testImplementation(kotlin("test"))
}



group = "com.zhangwh"
//version = "0.1.0"
//version = getVersionString("1.4.1")

repositories {
    mavenCentral()
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#intellij-extension-type
intellij {
//    version.set("LATEST-EAP-SNAPSHOT")
    version.set("2022.3.3")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(
        "Git4Idea",
    ))
}

val javaCompilerVersion = "17"

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = javaCompilerVersion
        targetCompatibility = javaCompilerVersion
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = javaCompilerVersion
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        channels.set(listOf(System.getenv("PUBLISH_CHANNEL")))
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test  {
        useJUnitPlatform()
    }

    register<Copy>("copyExecutable") {
        val targetFile = Paths.get("$buildDir/distributions/bin/g_pilot_idea_lsp.exe")

        // Before copying, check if the target file is locked or in use
        doFirst {
            if (Files.exists(targetFile)) {
                try {
                    // Try to delete the file to see if it's locked
                    Files.delete(targetFile)
                } catch (e: Exception) {
                    // File is locked or cannot be deleted, log a warning and skip the copy
                    logger.warn("The file $targetFile is in use and cannot be overwritten.")
                    throw StopExecutionException("Skipping copy since the file is in use.")
                }
            }
        }

        // Perform the actual copy if the file is not locked
        from("src/main/resources/bin/g_pilot_idea_lsp.exe")
        into("$buildDir/distributions/bin")
    }

    // Make sure the copyExecutable task runs before building the plugin
    named("buildPlugin") {
        dependsOn("copyExecutable")
    }
}

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 10,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String = ProcessBuilder(split("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)".toRegex()))
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .redirectError(ProcessBuilder.Redirect.PIPE)
    .start()
    .apply { waitFor(timeoutAmount, timeoutUnit) }
    .run {
        val error = errorStream.bufferedReader().readText().trim()
        if (error.isNotEmpty()) {
            throw Exception(error)
        }
        inputStream.bufferedReader().readText().trim()
    }

fun getVersionString(baseVersion: String): String {
    val tag = "git tag -l --points-at HEAD".runCommand(workingDir = rootDir)
    if (System.getenv("PUBLISH_EAP") != "1" &&
        tag.isNotEmpty() && tag.contains(baseVersion)) return baseVersion

    val branch = "git rev-parse --abbrev-ref HEAD".runCommand(workingDir = rootDir)
    val numberOfCommits = if (branch == "main") {
        val lastTag = "git describe --tags --abbrev=0 @^".runCommand(workingDir = rootDir)
        "git rev-list ${lastTag}..HEAD --count".runCommand(workingDir = rootDir)
    } else {
        "git rev-list --count HEAD ^origin/main".runCommand(workingDir = rootDir)
    }
    val commitId = "git rev-parse --short=8 HEAD".runCommand(workingDir = rootDir)
    return if (System.getenv("PUBLISH_EAP") == "1") {
        "$baseVersion.$numberOfCommits-eap-$commitId"
    } else {
        "$baseVersion-$branch-$numberOfCommits-$commitId"
    }
}
