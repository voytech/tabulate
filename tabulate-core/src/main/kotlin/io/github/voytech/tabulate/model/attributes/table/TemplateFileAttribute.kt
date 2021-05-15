package io.github.voytech.tabulate.model.attributes.table

import io.github.voytech.tabulate.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.model.attributes.TableAttribute

data class TemplateFileAttribute(val fileName: String): TableAttribute<TemplateFileAttribute>() {
    override fun mergeWith(other: TemplateFileAttribute): TemplateFileAttribute = other

    class Builder : TableAttributeBuilder {
        lateinit var fileName: String
        override fun build(): TableAttribute<TemplateFileAttribute> = TemplateFileAttribute(fileName)
    }
}

//@JvmName("jTemplate")
//fun <T> TableLevelAttributesBuilderApi<T>.template(block: TemplateFileAttribute.Builder.() -> Unit): TableAttribute<TemplateFileAttribute> = TemplateFileAttribute.Builder().apply(block).build()

fun <T> TableLevelAttributesBuilderApi<T>.template(block: TemplateFileAttribute.Builder.() -> Unit) {
    attribute(TemplateFileAttribute.Builder().apply(block).build())
}