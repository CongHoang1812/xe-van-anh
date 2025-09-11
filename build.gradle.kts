// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath(libs.navigation.safe.args.gradle.plugin)
    }
}