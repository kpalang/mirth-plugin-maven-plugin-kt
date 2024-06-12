package com.kaurpalang.mirth.annotationsplugin.annotation

import com.kaurpalang.mirth.annotationsplugin.type.ApiProviderType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class MirthApiProvider(
    val type: ApiProviderType
)
