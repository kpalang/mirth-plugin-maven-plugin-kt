package com.kaurpalang.mirth.annotationsplugin.annotation

import com.kaurpalang.mirth.annotationsplugin.type.LibraryType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class MirthLibrary(
    val type: LibraryType
)
