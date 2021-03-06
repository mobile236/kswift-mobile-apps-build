/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("detekt-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.mobile.multiplatform.ios-framework")
    id("dev.icerock.moko.kswift")
}

kotlin {
    android()
    ios()
    iosSimulatorArm64()

    sourceSets {
        val iosMain by getting
        val iosSimulatorArm64Main by getting

        iosSimulatorArm64Main.dependsOn(iosMain)
    }
}

kswift {
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature)
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)

    excludeLibrary("kotlinx-coroutines-core")

    projectPodspecName.set("MultiPlatformLibrary")
    iosDeploymentTarget.set("11.0")
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(projects.kswiftRuntime)
}

framework {
    export(libs.coroutines)
    export(projects.kswiftRuntime)
}
