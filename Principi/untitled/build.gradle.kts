import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.10" // Ensure this is the correct version
}

group = "si.um.feri.lpm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://androidx.dev/storage/compose-compiler/repository")
    maven("https://packages.jetbrains.team/maven/p/skija/maven")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation("com.google.code.gson:gson:2.8.8")

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.4.0")
    implementation("org.mongodb:mongodb-driver-sync:4.3.1")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("org.jetbrains.skija:skija-macos-arm64:0.93.1")
    implementation("io.ktor:ktor-client-core:2.2.1")
    implementation("io.ktor:ktor-client-cio:2.2.1")
    implementation("io.ktor:ktor-client-json:2.2.1")
    implementation("io.ktor:ktor-client-serialization:2.2.1")
    implementation("io.ktor:ktor-client-logging:2.2.1")
}

compose.desktop {
    application {
        mainClass = "com.yourpackage.gui.MainGuiKt" // Ensure this points to your main function

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "principiPJ"
            packageVersion = "1.0.0"
        }
    }

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
