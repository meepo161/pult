import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

application {
    mainClassName ="ru.avem.pult.app.MainApp"
}

group = "ru.avem"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes["Class-Path"] = configurations.compile.map {
            it.name
        }

        attributes["Main-Class"] = application.mainClassName
    }

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

@kotlin.Suppress("UNCHECKED_CAST")
tasks.register("syncGitlab") {
    doLast {
        val commitRequest = groovy.json.JsonSlurper().parseText(
            URL("http://192.168.1.200/api/v4/projects/90/repository/commits/master?private_token=tAvazqTnvsZhXeGcYwRC")
                .readText()
        ) as Map<*, *>
        val milestonesRequest = groovy.json.JsonSlurper().parseText(
            URL("http://192.168.1.200/api/v4/projects/90/milestones?state=active&private_token=tAvazqTnvsZhXeGcYwRC")
                .readText()
        ) as ArrayList<Map<String, *>>
        val milestoneVersion = milestonesRequest[0]["title"].toString()
        val commitHash = commitRequest["short_id"].toString()
        version = "${milestoneVersion}_${commitHash}"
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
    implementation("no.tornado:tornadofx:1.7.18")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.jetbrains.exposed:exposed:0.13.6")
    implementation("org.xerial:sqlite-jdbc:3.27.2.1")
    implementation("de.jensd:fontawesomefx:8.9")
    implementation("de.jensd:fontawesomefx-icons525:2.0-2")
    implementation("de.jensd:fontawesomefx-materialdesignfont:1.4.57-2")
    implementation("de.jensd:fontawesomefx-materialicons:2.1-2")
    implementation("de.jensd:fontawesomefx-octicons:3.3.0-2")
    implementation("de.jensd:fontawesomefx-weathericons:2.0-2")
    implementation("de.jensd:fontawesomefx-icons525:2.0-2")
    implementation("org.apache.poi:poi:4.1.0")
    implementation("org.apache.poi:poi-ooxml:4.1.0")
    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")
    implementation("org.apache.logging.log4j:log4j-api:2.9.1")
    implementation("org.apache.logging.log4j:log4j-core:2.9.1")
}

