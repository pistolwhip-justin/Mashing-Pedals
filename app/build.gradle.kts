plugins {
    id("com.android.application")
}
android {
    namespace = "com.pistolwhip.mashingpedals"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.pistolwhip.mashingpedals"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
