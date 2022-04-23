package io.github.voytech.tabulate.components.table.model.attributes.row

import io.github.voytech.tabulate.components.table.api.builder.RowAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

data class RowHeightAttribute(val px: Int) : RowAttribute<RowHeightAttribute>() {

    override fun overrideWith(other: RowHeightAttribute): RowHeightAttribute = RowHeightAttribute(
        px = takeIfChanged(other, RowHeightAttribute::px)
    )

    @TabulateMarker
    class Builder : RowAttributeBuilder<RowHeightAttribute>() {
        var px: Int by observable(-1)
        override fun provide(): RowHeightAttribute = RowHeightAttribute(px)
    }

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T> RowLevelAttributesBuilderApi<T>.height(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block))

fun <T> TableLevelAttributesBuilderApi<T>.rowHeight(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block))

