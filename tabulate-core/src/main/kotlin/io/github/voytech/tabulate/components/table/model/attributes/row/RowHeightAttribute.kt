package io.github.voytech.tabulate.components.table.model.attributes.row

import io.github.voytech.tabulate.components.table.api.builder.RowAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundaries
import io.github.voytech.tabulate.core.template.layout.elementBoundaries

data class RowHeightAttribute(val px: Int) : RowAttribute<RowHeightAttribute>(), LayoutElement {

    override fun overrideWith(other: RowHeightAttribute): RowHeightAttribute = RowHeightAttribute(
        px = takeIfChanged(other, RowHeightAttribute::px)
    )

    @TabulateMarker
    class Builder : RowAttributeBuilder<RowHeightAttribute>() {
        var px: Int by observable(-1)
        override fun provide(): RowHeightAttribute = RowHeightAttribute(px)
    }

    override fun Layout.computeBoundaries(): LayoutElementBoundaries =
        query.elementBoundaries(height = Height(value = px.toFloat(), UnitsOfMeasure.PX))

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T: Any> RowLevelAttributesBuilderApi<T>.height(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.rowHeight(block: RowHeightAttribute.Builder.() -> Unit) =
    attribute(RowHeightAttribute.Builder().apply(block))

