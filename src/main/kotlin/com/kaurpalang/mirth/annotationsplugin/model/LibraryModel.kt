package com.kaurpalang.mirth.annotationsplugin.model

import kotlinx.serialization.Serializable

@Serializable
data class LibraryModel(
    val type: String,
    val path: String,
)
