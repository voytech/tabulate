package io.github.voytech.tabulate.model.attributes.row

import io.github.voytech.tabulate.api.builder.RowAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.model.attributes.RowAttribute

data class RowHeightAttribute(val px: Int) : RowAttribute<RowHeightAttribute>() {

    override fun overrideWith(other: RowHeightAttribute): RowHeightAttribute = RowHeightAttribute(
        px = takeIfChanged(other, RowHeightAttribute::px)
    )

    @TabulateMarker
    class Builder : RowAttributeBuilder() {
        var px: Int by observable(-1)
        override fun provide(): RowAttribute<RowHeightAttribute> = RowHeightAttribute(px)
    }

}

fun <T> RowLevelAttributesBuilderApi<T>.height(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block).build())

fun <T> TableLevelAttributesBuilderApi<T>.rowHeight(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block).build())

