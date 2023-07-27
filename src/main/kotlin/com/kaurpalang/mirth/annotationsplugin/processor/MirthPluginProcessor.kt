package com.kaurpalang.mirth.annotationsplugin.processor

import com.google.auto.service.AutoService
import com.kaurpalang.mirth.annotationsplugin.annotation.MirthApiProvider
import com.kaurpalang.mirth.annotationsplugin.annotation.MirthClientClass
import com.kaurpalang.mirth.annotationsplugin.annotation.MirthServerClass
import com.kaurpalang.mirth.annotationsplugin.config.Constants
import com.kaurpalang.mirth.annotationsplugin.model.ApiProviderModel
import com.kaurpalang.mirth.annotationsplugin.model.PluginState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

private val json = Json

@AutoService(Processor::class)
class MirthPluginProcessor : AbstractProcessor() {

    private var messager: Messager? = null

    private val serverClasses = mutableSetOf<String>()
    private val clientClasses = mutableSetOf<String>()
    private val apiProviders = mutableSetOf<ApiProviderModel>()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        processClasses(roundEnv, MirthServerClass::class.java, serverClasses)
        processClasses(roundEnv, MirthClientClass::class.java, clientClasses)
        processApiProviders(roundEnv)

        if (roundEnv.processingOver()) handleEndOfProcessing()
        return false
    }

    /**
     * Method iterates over classes with param annotation and adds them to specified target set
     *
     * @param roundEnv Round environment
     * @param annotation Annotation to process
     * @param targetSet Set to add found classes to
     */
    private fun processClasses(
        roundEnv: RoundEnvironment,
        annotation: Class<out Annotation?>,
        targetSet: MutableSet<String>
    ) {
        for (annotatedElement in roundEnv.getElementsAnnotatedWith(annotation)) {
            if (!annotatedElement.kind.isClass) {
                error(annotatedElement, "Only classes can be annotated with @${annotation.simpleName}")
            } else {
                targetSet.add(annotatedElement.asType().toString())
            }
        }
    }

    /**
     * Method iterates over apiProvider classes, and adds them to set
     *
     * @param roundEnv Round environment
     */
    private fun processApiProviders(roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(MirthApiProvider::class.java).forEach { element -> run {
            if (!element.kind.isClass && !element.kind.isInterface) {
                error(element, "Only classes or interfaces can be annotated with @${MirthApiProvider::class.java.simpleName}. Type ${element.kind} is not applicable")
            } else {
                val apiProvider = element.getAnnotation(MirthApiProvider::class.java)
                apiProviders.add(
                    ApiProviderModel(apiProvider.type, element.asType().toString())
                )
            }
        } }
    }

    /**
     * This method runs after current processing round has completed.
     * Method writes all found server-, client- and apiprovider classes to aggregation json file to
     * be stored for plugin.xml generation.
     */
    private fun handleEndOfProcessing() {
        try {
            val aggregationFile = File(Constants.AGGREGATION_FILE_PATH)

            val pluginState = if (aggregationFile.exists()) {
                json.decodeFromString(aggregationFile.readText())
            } else {
                PluginState()
            }

            pluginState.serverClasses.addAll(serverClasses)
            pluginState.clientClasses.addAll(clientClasses)
            pluginState.apiProviders.addAll(apiProviders)

            aggregationFile.writeText(json.encodeToString(pluginState))
        } catch (exception: Exception) {
            messager!!.printMessage(Diagnostic.Kind.ERROR, exception.message)
        }
    }

    override fun getSupportedAnnotationTypes() = setOf(
        MirthServerClass::class.java.canonicalName,
        MirthClientClass::class.java.canonicalName,
        MirthApiProvider::class.java.canonicalName,
    )

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

    private fun error(element: Element, message: String) {
        messager!!.printMessage(Diagnostic.Kind.ERROR, message, element)
    }
}