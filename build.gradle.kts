import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "org.zozhaura"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.5"

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("com.clickhouse:clickhouse-jdbc:0.4.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.auth0:java-jwt:4.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "20"
}

tasks.register("runUserImitation", JavaExec::class) {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("userImitation.UserImitationMicroserviceKt")
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.register("runProxy", JavaExec::class) {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("proxy.ProxyMicroserviceKt")
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.register("runFood", JavaExec::class) {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("food.FoodMicroserviceKt")
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.register("runLogs", JavaExec::class) {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("logs.LogsMicroserviceKt")
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

application {
    mainClass.set("MainKt")
}