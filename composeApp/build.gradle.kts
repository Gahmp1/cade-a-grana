import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(project(":shared"))
            implementation(libs.kotlinx.datetime)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "com.financehub"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.financehub"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "FinanceHub"
            packageVersion = "1.0.0"
            description = "Personal Finance Management App"
            vendor = "FinanceHub"
            
            // Include all Java modules for full JDK support
            includeAllModules = true
            
            // Include CLI JAR in the app bundle
            appResourcesRootDir.set(project.layout.projectDirectory.dir("bundle-resources"))

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
                bundleID = "com.financehub"
            }
            
            windows {
                menuGroup = "FinanceHub"
                upgradeUuid = "B9F8E2D4-3A1C-4F5E-8D7B-6C9A0E1F2D3B"
            }
            
            linux {
                packageName = "financehub"
                debMaintainer = "financehub@example.com"
            }
        }
    }
}

// Task to copy CLI JAR to bundle resources
val copyCliJarToBundle by tasks.registering(Copy::class) {
    dependsOn(":cli:shadowJar")
    from(project(":cli").layout.buildDirectory.file("libs/fhub-1.0.0.jar"))
    into(layout.projectDirectory.dir("bundle-resources/macos"))
    rename { "fhub-cli.jar" }
}

// Ensure CLI JAR is copied before packaging
tasks.matching { 
    it.name.contains("packageDmg") || 
    it.name.contains("packageMsi") ||
    it.name.contains("packageDeb") ||
    it.name.contains("createDistributable") || 
    it.name == "prepareAppResources" 
}.configureEach {
    dependsOn(copyCliJarToBundle)
}

// Ensure CLI JAR is built before running the app
afterEvaluate {
    tasks.findByName("run")?.dependsOn(":cli:shadowJar")
    tasks.findByName("runDistributable")?.dependsOn(":cli:shadowJar")
}
