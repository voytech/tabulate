package io.github.voytech.tabulate.model.attributes.table

import io.github.voytech.tabulate.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.model.attributes.TableAttribute

data class TemplateFileAttribute(val fileName: String): TableAttribute<TemplateFileAttribute>() {

    override fun mergeWith(other: TemplateFileAttribute): TemplateFileAttribute = TemplateFileAttribute(
        fileName = takeIfChanged(other, TemplateFileAttribute::fileName)
    )

    @TabulateMarker
    class Builder : TableAttributeBuilder() {
        var fileName: String? by observable(null)
        override fun provide(): TableAttribute<TemplateFileAttribute> =
            TemplateFileAttribute(fileName ?: throw BuilderException("fileName must be provided for TemplateFileAttribute"))
    }
}

fun <T> TableLevelAttributesBuilderApi<T>.template(block: TemplateFileAttribute.Builder.() -> Unit) {
    attribute(TemplateFileAttribute.Builder().apply(block).build())
}