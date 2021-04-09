package pl.voytech.exporter.core.model.attributes.row

import pl.voytech.exporter.core.api.builder.RowAttributeBuilder
import pl.voytech.exporter.core.model.attributes.RowAttribute

data class RowHeightAttribute(val px: Int) : RowAttribute<RowHeightAttribute>() {

    override fun mergeWith(other: RowHeightAttribute): RowHeightAttribute = other

    class Builder : RowAttributeBuilder {
        var px: Int = -1
        override fun build(): RowAttribute<RowHeightAttribute> = RowHeightAttribute(px)
    }

}

fun height(block: RowHeightAttribute.Builder.() -> Unit): RowAttribute<RowHeightAttribute> = RowHeightAttribute.Builder().apply(block).build()
