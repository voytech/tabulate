package io.github.voytech.tabulate.components.document.api.builder.dsl

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute

fun DocumentLevelAttributesBuilderApi.width(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder<Document>().apply(block))

fun DocumentLevelAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder<Document>().apply(block))