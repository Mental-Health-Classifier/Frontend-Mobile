import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// Signing release: kredensial dibaca dari keystore/keystore.properties (gitignored).
// Kalau file ini tidak ada (misal di mesin developer lain / CI), release build tetap
// jalan tanpa signing config (hasilnya APK unsigned, sama seperti sebelumnya).
val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}

android {
    namespace = "com.example.mindcare"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.mindcare"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = (project.findProperty("MINDCARE_VERSION_CODE") as String).toInt()
        versionName = project.findProperty("MINDCARE_VERSION_NAME") as String
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// AGP 9.0.1 tidak lagi mengekspos VariantOutput.outputFileName secara publik,
// jadi rename dilakukan via Copy task terpisah yang baca hasil dari Artifacts API.
extensions.getByType<ApplicationAndroidComponentsExtension>().onVariants { variant ->
    val versionName = project.findProperty("MINDCARE_VERSION_NAME") as String
    val variantName = variant.name
    val variantNameCapitalized = variantName.replaceFirstChar { it.uppercase() }

    val renameApkTask = tasks.register<Copy>("renameApk$variantNameCapitalized") {
        from(variant.artifacts.get(SingleArtifact.APK))
        include("*.apk")
        into(layout.buildDirectory.dir("outputs/apk-named/$variantName"))
        rename { "MindCare-$versionName-$variantName.apk" }
    }

    afterEvaluate {
        tasks.findByName("assemble$variantNameCapitalized")?.finalizedBy(renameApkTask)
    }
}