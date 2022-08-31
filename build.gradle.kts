import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    kotlin("kapt") version "1.7.10"
}

application {
    mainClass.set("com.anksystems.fenomy_pushk.FenomyPushkApplicationKt")
}

group = "com.anksystems"
version = "0.0.2"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    google()
}


dependencies {
    //implementation("org.springframework.boot:spring-boot-starter-mail")
    //implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    //implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    //implementation("org.projectlombok:lombok:1.18.24")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")

//    implementation("org.postgresql:postgresql")
//    runtimeOnly("org.postgresql:postgresql:")

    val exposedVer = "0.39.2"
    implementation("org.jetbrains.exposed:exposed-core:0.39.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.39.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.39.2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.39.2")

    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("com.google.firebase:firebase-admin:9.0.0")
    //implementation("com.google.gms:google-services:4.3.13")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    val configurationProcessor ="org.springframework.boot:spring-boot-configuration-processor:2.7.2"
    kapt(configurationProcessor)
    annotationProcessor(configurationProcessor)

    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    launchScript()
}

tasks.create("jarPath") {
    println("$archivesName-$version.jar")
}