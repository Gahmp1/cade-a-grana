plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.financehub"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Shared module for business logic
    implementation(project(":shared"))
    
    // Clikt CLI framework
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    
    // Kotlinx Serialization for JSON export
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Kotlinx DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    
    // Testing
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.financehub.cli.MainKt")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    shadowJar {
        archiveBaseName.set("fhub")
        archiveClassifier.set("")
        archiveVersion.set("1.0.0")
        
        manifest {
            attributes(
                "Main-Class" to "com.financehub.cli.MainKt",
                "Implementation-Title" to "FinanceHub CLI",
                "Implementation-Version" to "1.0.0"
            )
        }
        
        // Minimize JAR by excluding unused classes
        mergeServiceFiles()
    }
    
    // Make build depend on shadowJar
    build {
        dependsOn(shadowJar)
    }
    
    test {
        useJUnitPlatform()
    }
}
