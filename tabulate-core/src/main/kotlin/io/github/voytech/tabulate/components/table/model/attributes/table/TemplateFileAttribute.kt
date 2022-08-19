package io.github.voytech.tabulate.components.table.model.attributes.table

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.exception.BuilderException
import io.github.voytech.tabulate.components.table.operation.TableContext
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute

data class TemplateFileAttribute(val fileName: String): Attribute<TemplateFileAttribute>() {

    override fun overrideWith(other: TemplateFileAttribute): TemplateFileAttribute = TemplateFileAttribute(
        fileName = takeIfChanged(other, TemplateFileAttribute::fileName)
    )

    @TabulateMarker
    class Builder : AttributeBuilder<TemplateFileAttribute>(TableContext::class.java) {
        var fileName: String? by observable(null)
        override fun provide(): TemplateFileAttribute =
            TemplateFileAttribute(fileName ?: throw BuilderException("fileName must be provided for TemplateFileAttribute"))
    }

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }

}

fun <T: Any> TableLevelAttributesBuilderApi<T>.template(block: TemplateFileAttribute.Builder.() -> Unit) {
    attribute(TemplateFileAttribute.Builder().apply(block))
}