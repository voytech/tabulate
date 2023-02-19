package io.github.voytech.tabulate.core.model

interface AttributeConstraintsProvider {
    fun defineConstraints(): AttributeConstraintsBuilder.() -> Unit
}
