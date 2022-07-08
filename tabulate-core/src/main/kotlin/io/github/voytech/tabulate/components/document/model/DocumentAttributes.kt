package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.core.model.Attribute

abstract class DocumentAttribute<T: DocumentAttribute<T>>: Attribute<T>()
