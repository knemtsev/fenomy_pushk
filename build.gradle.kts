import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.anksystems"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    google()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    //implementation("org.projectlombok:lombok:1.18.24")
    runtimeOnly("org.postgresql:postgresql")

    val exposedVer = "0.39.2"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVer")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVer")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVer")

    implementation("com.google.firebase:firebase-admin:9.0.0")
    //implementation("com.google.gms:google-services:4.3.13")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
