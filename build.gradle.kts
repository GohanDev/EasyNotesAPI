plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "pt.ipt.easynotes"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    // Ktor
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)

    // Autenticação JWT
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)

    // Base de dados
    implementation("org.jetbrains.exposed:exposed-core:1.4.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.4.0")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.postgresql:postgresql:42.7.7")

    // Hash das passwords
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Documentação da API
    implementation("io.ktor:ktor-server-swagger:3.5.0")

    // Testes
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}