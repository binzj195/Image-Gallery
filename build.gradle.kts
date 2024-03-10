// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
}
buildscript {
    repositories {
        mavenCentral()
        google()
        flatDir {
            dirs("libs")
        }
        jcenter()
        // Add any required repositories here
    }
    dependencies {
        // Add the classpath dependency
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
        classpath("com.google.gms:google-services:4.3.3")
    }
}
allprojects {
    repositories {


        // Add any required repositories here
    }

}
