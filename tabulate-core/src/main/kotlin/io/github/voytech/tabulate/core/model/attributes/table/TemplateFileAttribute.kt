package io.github.voytech.tabulate.core.model.attributes.table

import io.github.voytech.tabulate.core.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.core.model.attributes.TableAttribute

data class TemplateFileAttribute(val fileName: String): TableAttribute<TemplateFileAttribute>() {
    override fun mergeWith(other: TemplateFileAttribute): TemplateFileAttribute = other

    class Builder : TableAttributeBuilder {
        lateinit var fileName: String
        override fun build(): TableAttribute<TemplateFileAttribute> = TemplateFileAttribute(fileName)
    }
}

fun template(block: TemplateFileAttribute.Builder.() -> Unit): TableAttribute<TemplateFileAttribute> = TemplateFileAttribute.Builder().apply(block).build()

