import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.10" // Ensure this is the correct version
}

group = "si.um.feri.lpm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
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
