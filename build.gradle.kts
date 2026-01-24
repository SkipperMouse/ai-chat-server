plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.sokolov"
version = "0.0.1-SNAPSHOT"
description = "ai-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama:1.1.2")
    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc:1.1.2")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector:1.1.2")
    implementation("org.springframework.ai:spring-ai-advisors-vector-store:1.1.2")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.flywaydb:flyway-core:11.20.1")
    implementation("org.apache.lucene:lucene-core:10.3.2")
    implementation("org.apache.lucene:lucene-analysis-common:10.3.2")
    implementation("com.github.pemistahl:lingua:1.2.2")
    runtimeOnly("org.postgresql:postgresql:42.7.8")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.20.1")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
