package com.kaurpalang.mirth.annotationsplugin.mojo

import com.kaurpalang.mirth.annotationsplugin.config.Constants
import com.kaurpalang.mirth.annotationsplugin.model.ApiProviderModel
import com.kaurpalang.mirth.annotationsplugin.model.LibraryModel
import com.kaurpalang.mirth.annotationsplugin.model.PluginState
import kotlinx.serialization.json.Json
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

private val json = Json

@Mojo(name = "generate-plugin-xml", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
class PluginXMLGeneratorMojo : AbstractMojo() {
    @Parameter(defaultValue = "\${project}")
    private val project: MavenProject? = null

    @Parameter(property = "name", defaultValue = "default_name")
    private val name: String? = null

    @Parameter(property = "author", defaultValue = "default_author")
    private val author: String? = null

    @Parameter(property = "pluginVersion", defaultValue = "default_plugin_version")
    private val pluginVersion: String? = null

    @Parameter(property = "mirthVersion", defaultValue = "default_mirth_version")
    private val mirthVersion: String? = null

    @Parameter(property = "url")
    private val url: String? = null

    @Parameter(property = "description")
    private val description: String? = null

    @Parameter(property = "path", defaultValue = "default_path")
    private val path: String? = null

    @Parameter(property = "pluginXmlOutputPath", defaultValue = "plugin.xml")
    private val pluginXmlOutputPath: String = "plugin.xml"

    override fun execute() {
        val aggregatorFile = File(Constants.AGGREGATION_FILE_PATH)

        if (!aggregatorFile.exists()) {
            log.error("Aggregator file does not exist at ${aggregatorFile.absolutePath}");
            return;
        }

        val pluginState = json.decodeFromString<PluginState>(aggregatorFile.readText(Charsets.UTF_8))
        pluginState.runtimeClientLibraries.addAll(getRuntimeLibrariesList("client"))
        pluginState.runtimeSharedLibraries.addAll(getRuntimeLibrariesList("shared"))
        pluginState.runtimeServerLibraries.addAll(getRuntimeLibrariesList("client"))

        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val document = documentBuilder.newDocument()

        // Creating the root element
        val rootElement = document.createElement("pluginMetaData")
        rootElement.setAttribute("path", path)
        document.appendChild(rootElement)

        // Append elements for plugin details
        rootElement.appendChild(getSimpleChildElement(document, "name", name))
        rootElement.appendChild(getSimpleChildElement(document, "author", author))
        rootElement.appendChild(getSimpleChildElement(document, "pluginVersion", pluginVersion))
        rootElement.appendChild(getSimpleChildElement(document, "mirthVersion", mirthVersion))
        rootElement.appendChild(getSimpleChildElement(document, "url", url))
        rootElement.appendChild(getSimpleChildElement(document, "description", description))

        // Add client classes
        if (pluginState.clientClasses.isNotEmpty()) {
            rootElement.appendChild(getClassesChildElement(document, "clientClasses", pluginState.clientClasses))
        }

        // Add server classes
        if (pluginState.serverClasses.isNotEmpty()) {
            rootElement.appendChild(getClassesChildElement(document, "serverClasses", pluginState.serverClasses))
        }

        // Add libraries
        val mirthPluginProject = project!!.parent
        val mirthPluginModules = mirthPluginProject.modules
            .filter { s -> s != project.artifactId }
            .toSet()

        val mirthPluginBuildDir = mirthPluginProject.basedir.toPath()

        // I can't remember what this is for...
        mirthPluginModules.forEach { module -> run {
            val mirthPluginSubmoduleDirectory = Path(mirthPluginBuildDir.toString(), module, "target")
            if (!mirthPluginSubmoduleDirectory.exists()) return@forEach

            mirthPluginSubmoduleDirectory.toFile()
                .walkTopDown()
                .filter { file -> file.name == "${mirthPluginProject.artifactId}-$module.jar" }
                .forEach { jarFile -> run {
                    val libraryModel = LibraryModel(module.uppercase(), jarFile.name)
                    rootElement.appendChild(getLibraryChildElement(document, libraryModel))
                } }
        } }

        // Add runtime libraries
        pluginState.runtimeClientLibraries.forEach { library -> rootElement.appendChild(getLibraryChildElement(document, library))}
        pluginState.runtimeSharedLibraries.forEach { library -> rootElement.appendChild(getLibraryChildElement(document, library))}
        pluginState.runtimeServerLibraries.forEach { library -> rootElement.appendChild(getLibraryChildElement(document, library))}

        // Add API providers
        pluginState.apiProviders.forEach { apiProvider -> rootElement.appendChild(getApiProvidersChildElement(document, apiProvider)) }


        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        val domSource = DOMSource(document)
        val streamResult = StreamResult(File(pluginXmlOutputPath))
        transformer.transform(domSource, streamResult)

        if (aggregatorFile.exists() && aggregatorFile.delete()) {
            log.info("Aggregation file deleted")
        } else {
            log.warn("Aggregation file not deleted from ${aggregatorFile.absolutePath}. You should delete it manually to avoid conflicts!");
        }
    }

    private fun getRuntimeLibrariesList(submodule: String): Set<LibraryModel> {
        val submoduleLibrariesDirectory = Path(
            project!!.parent.basedir.absolutePath,
            "libs",
            "runtime",
            submodule
        )

        // Return if libraries directory doesn't exist
        if (!submoduleLibrariesDirectory.exists()) {
            return emptySet()
        }

        // Return if libraries path is not a directory
        if (!submoduleLibrariesDirectory.isDirectory()) {
            throw InvalidPathException(submoduleLibrariesDirectory.absolutePathString(), "Path is not a directory")
        }

        return submoduleLibrariesDirectory.toFile()
            .walkTopDown()
            .filter { file -> file.isFile }
            .filter { file -> file.endsWith(".jar") }
            .sorted()
            .map { file -> LibraryModel(submodule.uppercase(), "libs/${file.name}") }
            .toSet()
    }

    private fun getSimpleChildElement(document: Document, tagName: String, textContent: String?): Element {
        val childElement = document.createElement(tagName)
        childElement.textContent = textContent
        return childElement
    }

    private fun getLibraryChildElement(document: Document, libraryModel: LibraryModel): Element {
        val childElement = document.createElement("library")
        childElement.setAttribute("type", libraryModel.type)
        childElement.setAttribute("path", libraryModel.path)
        return childElement
    }

    private fun getApiProvidersChildElement(document: Document, apiProviderModel: ApiProviderModel): Element {
        val childElement = document.createElement("apiProvider")
        childElement.setAttribute("type", apiProviderModel.type.toString())
        childElement.setAttribute("name", apiProviderModel.name)
        return childElement
    }

    private fun getClassesChildElement(document: Document, rootTagName: String, classesSet: Set<String>): Element {
        val classesElement = document.createElement(rootTagName)
        classesSet.forEach { serverClass -> classesElement.appendChild(getSimpleChildElement(document, "string", serverClass)) }
        return classesElement
    }
}