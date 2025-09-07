plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
}

android {
  namespace = "com.nossoguiadecompras"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.nossoguiadecompras"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }

  // >>> Alinhar Java para 17
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  // >>> Alinhar Kotlin para 17
  kotlinOptions {
    jvmTarget = "17"
  }
  // >>> (Opcional, recomendado) Toolchain
  kotlin {
    jvmToolchain(17)
  }

  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.navigation:navigation-compose:2.7.7")

  // Material Components para o tema XML Material3
  implementation("com.google.android.material:material:1.12.0")

  // Firebase
  implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
  implementation("com.google.firebase:firebase-analytics-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.google.firebase:firebase-auth-ktx")
}
