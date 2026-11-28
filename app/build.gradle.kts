import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

// Read Supabase configuration from local.properties.
val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

val supabaseUrl = localProperties.getProperty("SUPABASE_URL", "")
val supabasePublishableKey =
    localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", "")

fun escapeBuildConfigValue(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

android {
    namespace = "com.example.tripmate"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tripmate"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Expose configuration through BuildConfig.
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${escapeBuildConfigValue(supabaseUrl)}\""
        )

        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            "\"${escapeBuildConfigValue(supabasePublishableKey)}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Existing Android dependencies
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Supabase BOM and modules
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)

    // Ktor Android networking
    implementation(libs.ktor.client.android)

    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // Android compatibility for minSdk 24
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Existing testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
