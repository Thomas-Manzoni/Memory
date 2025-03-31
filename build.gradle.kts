// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Use AGP 8.5.2 to match the supported Gradle version
        classpath("com.android.tools.build:gradle:8.5.2")
    }
}