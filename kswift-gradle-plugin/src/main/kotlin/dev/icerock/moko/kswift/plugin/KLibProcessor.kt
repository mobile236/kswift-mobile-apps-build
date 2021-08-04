/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.FileSpec
import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.util.Logger
import java.io.File
import kotlin.reflect.KClass

class KLibProcessor(
    private val logger: Logger,
    config: Builder.() -> Unit
) {
    private val features: Map<KClass<out FeatureContext>, List<ProcessorFeature<*>>> =
        Builder().apply(config).build()

    fun processFeatureContext(library: File, outputDir: File, framework: Framework) {
        val metadata: KlibModuleMetadata =
            KotlinMetadataLibraryProvider.readLibraryMetadata(library)

        logger.log("metadata $metadata")

        val fileSpecBuilder: FileSpec.Builder = FileSpec.builder(library.nameWithoutExtension)

        val processorContext = ProcessorContext(
            fileSpecBuilder = fileSpecBuilder,
            framework = framework
        )

        val libraryContext = LibraryContext(metadata)
        libraryContext.visit { processFeatureContext(this, processorContext) }

        val fileSpec: FileSpec = fileSpecBuilder.build()
        if (fileSpec.members.isNotEmpty()) fileSpec.writeTo(outputDir)
    }

    private fun <T : FeatureContext> processFeatureContext(
        featureContext: T,
        processorContext: ProcessorContext
    ) {
        val kclass: KClass<out T> = featureContext::class
        val processors: List<ProcessorFeature<T>> =
            features[kclass].orEmpty() as List<ProcessorFeature<T>>
        processors.forEach { it.process(featureContext, processorContext) }
    }

    class Builder {
        private val features = mutableMapOf<KClass<out FeatureContext>, List<ProcessorFeature<*>>>()

        fun <CTX : FeatureContext> install(
            clazz: KClass<CTX>,
            processor: ProcessorFeature<CTX>
        ) {
            val currentList: List<ProcessorFeature<*>> = features[clazz] ?: emptyList()
            features[clazz] = currentList.plus(processor)
        }

        inline fun <reified CTX : FeatureContext> install(processor: ProcessorFeature<CTX>) {
            install(CTX::class, processor)
        }

        fun build() = features.toMap()
    }
}
