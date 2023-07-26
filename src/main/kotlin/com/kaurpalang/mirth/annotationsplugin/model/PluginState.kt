package com.kaurpalang.mirth.annotationsplugin.model

import kotlinx.serialization.Serializable

@Serializable
internal data class PluginState(
    val serverClasses: MutableSet<String> = mutableSetOf(),
    val clientClasses: MutableSet<String> = mutableSetOf(),
    val apiProviders: MutableSet<ApiProviderModel> = mutableSetOf(),

    val runtimeClientLibraries: MutableSet<LibraryModel> = mutableSetOf(),
    val runtimeSharedLibraries: MutableSet<LibraryModel> = mutableSetOf(),
    val runtimeServerLibraries: MutableSet<LibraryModel> = mutableSetOf(),
)