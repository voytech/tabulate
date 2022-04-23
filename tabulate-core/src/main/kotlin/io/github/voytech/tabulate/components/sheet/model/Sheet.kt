package io.github.voytech.tabulate.components.sheet.model

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Model

class Sheet internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val node: Model? = null,
) : Model {
    override fun getId(): String = id
}

abstract class SheetAttribute<T : SheetAttribute<T>> : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<SheetAttribute<*>, Sheet> = AttributeClassifier.classify()
}
