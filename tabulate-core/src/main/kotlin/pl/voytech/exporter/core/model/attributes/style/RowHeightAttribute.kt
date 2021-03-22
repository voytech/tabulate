package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.RowAttributeBuilder
import pl.voytech.exporter.core.model.attributes.RowAttribute

data class RowHeightAttribute(val height: Int) : RowAttribute<RowHeightAttribute>() {

    override fun mergeWith(other: RowHeightAttribute): RowHeightAttribute = other

    class Builder : RowAttributeBuilder {
        var height: Int = -1
        override fun build(): RowAttribute<RowHeightAttribute> = RowHeightAttribute(height)
    }

}

fun height(block: RowHeightAttribute.Builder.() -> Unit): RowAttribute<RowHeightAttribute> = RowHeightAttribute.Builder().apply(block).build()
