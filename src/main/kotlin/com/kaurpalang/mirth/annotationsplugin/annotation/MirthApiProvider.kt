package com.kaurpalang.mirth.annotationsplugin.annotation

import com.kaurpalang.mirth.annotationsplugin.type.ApiProviderType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MirthApiProvider(
    val type: ApiProviderType
)
