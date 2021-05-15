package io.github.voytech.tabulate.model.attributes.row

import io.github.voytech.tabulate.api.builder.RowAttributeBuilder
import io.github.voytech.tabulate.model.attributes.RowAttribute

data class RowHeightAttribute(val px: Int) : RowAttribute<RowHeightAttribute>() {

    override fun mergeWith(other: RowHeightAttribute): RowHeightAttribute = other

    class Builder : RowAttributeBuilder {
        var px: Int = -1
        override fun build(): RowAttribute<RowHeightAttribute> = RowHeightAttribute(px)
    }

}

fun height(block: RowHeightAttribute.Builder.() -> Unit): RowAttribute<RowHeightAttribute> = RowHeightAttribute.Builder().apply(block).build()
