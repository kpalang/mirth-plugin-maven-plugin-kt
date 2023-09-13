package com.kaurpalang.mirth.annotationsplugin.model

import com.kaurpalang.mirth.annotationsplugin.type.ApiProviderType
import kotlinx.serialization.Serializable

@Serializable
data class ApiProviderModel(
    val type: ApiProviderType,
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ApiProviderModel) {
            return false
        }

        return other.name == this.name && other.type.compareTo(this.type) == 0
    }

    override fun hashCode() = name.hashCode() + type.hashCode()
    override fun toString() = "$type, $name"
}
