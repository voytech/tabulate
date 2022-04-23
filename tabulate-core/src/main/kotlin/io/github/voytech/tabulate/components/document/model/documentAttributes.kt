package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier

abstract class DocumentAttribute<T : DocumentAttribute<T>>  : Attribute<T>() {
    override fun getClassifier(): AttributeClassifier<DocumentAttribute<*>,Document> =
        AttributeClassifier.classify()

}

