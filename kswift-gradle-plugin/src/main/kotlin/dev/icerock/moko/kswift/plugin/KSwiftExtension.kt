/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import dev.icerock.moko.kswift.plugin.feature.ProcessorFeature
import kotlin.reflect.KClass

open class KSwiftExtension {
    internal val features: MutableMap<KClass<out FeatureContext>, List<ProcessorFeature<*>>> =
        mutableMapOf()

    internal val excludedLibs = mutableListOf<String>()
    internal val includedLibs = mutableListOf<String>()

    fun <CTX : FeatureContext, F : ProcessorFeature<CTX>, Config> install(
        featureContext: KClass<out CTX>,
        featureFactory: ProcessorFeature.Factory<CTX, F, Config>,
        config: Config.() -> Unit = {}
    ) {
        val currentList: List<ProcessorFeature<*>> = features[featureContext] ?: emptyList()
        val processorFeature: ProcessorFeature<CTX> = featureFactory.create(config)
        features[featureContext] = currentList.plus(processorFeature)
    }

    inline fun <reified CTX : FeatureContext, F : ProcessorFeature<CTX>, Config> install(
        featureFactory: ProcessorFeature.Factory<CTX, F, Config>,
        noinline config: Config.() -> Unit = {}
    ) {
        install(CTX::class, featureFactory, config)
    }

    fun <CTX : FeatureContext> excludeFilter(vararg names: String): ProcessorFeature.Filter.Exclude<CTX> {
        return ProcessorFeature.Filter.Exclude(names.toSet())
    }

    fun <CTX : FeatureContext> includeFilter(vararg names: String): ProcessorFeature.Filter.Include<CTX> {
        return ProcessorFeature.Filter.Include(names.toSet())
    }

    fun includeLibrary(libraryName: String) {
        includedLibs.add(libraryName)
    }

    fun excludeLibrary(libraryName: String) {
        excludedLibs.add(libraryName)
    }

    lateinit var projectPodspecName: String
}