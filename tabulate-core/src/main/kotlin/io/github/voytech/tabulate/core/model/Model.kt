package io.github.voytech.tabulate.core.model

interface Model {
    fun getId(): String

    fun getExplicitAttributeCategories(): Set<Class<out Attribute<*>>> = emptySet()
}