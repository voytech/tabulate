package io.github.voytech.tabulate.components.table.model.attributes.table

import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.components.table.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

data class TemplateFileAttribute(val fileName: String): TableAttribute<TemplateFileAttribute>() {

    override fun overrideWith(other: TemplateFileAttribute): TemplateFileAttribute = TemplateFileAttribute(
        fileName = takeIfChanged(other, TemplateFileAttribute::fileName)
    )

    @TabulateMarker
    class Builder : TableAttributeBuilder<TemplateFileAttribute>() {
        var fileName: String? by observable(null)
        override fun provide(): TemplateFileAttribute =
            TemplateFileAttribute(fileName ?: throw BuilderException("fileName must be provided for TemplateFileAttribute"))
    }

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }

}

fun <T> TableLevelAttributesBuilderApi<T>.template(block: TemplateFileAttribute.Builder.() -> Unit) {
    attribute(TemplateFileAttribute.Builder().apply(block))
}