package io.github.voytech.tabulate.components.table.model.attributes.column

import io.github.voytech.tabulate.components.table.api.builder.ColumnAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundaries
import io.github.voytech.tabulate.core.template.layout.elementBoundaries

enum class LengthUnit {
    PIXEL,
}

data class ColumnWidthAttribute(
    val auto: Boolean = false,
    val px: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL, // TODO remove or incorporate.
) : ColumnAttribute<ColumnWidthAttribute>(), LayoutElement {

    @TabulateMarker
    class Builder : ColumnAttributeBuilder<ColumnWidthAttribute>() {
        var auto: Boolean by observable(false)
        var px: Int by observable(-1)
        var unit: LengthUnit by observable(LengthUnit.PIXEL)
        override fun provide(): ColumnWidthAttribute = ColumnWidthAttribute(auto, px, unit)
    }

    /**
     * When overriding 'width' attribute:
     * 1) if only 'px' property has changed and its value is greater than 0 that means one want to
     * disable 'auto' property.
     * 2) if only 'auto' property has changed - take its new value. Regardless of 'px' property value 'auto' property
     * should force automatic width resolution.
     */
    override fun overrideWith(other: ColumnWidthAttribute): ColumnWidthAttribute =
        takeIfChanged(other, ColumnWidthAttribute::px).let { _px ->
            ColumnWidthAttribute(
                px = _px,
                auto = takeIfChanged(other, ColumnWidthAttribute::auto).let { _auto ->
                    if (_px != px && _px > 0 && _auto == auto) false else _auto
                },
                unit = takeIfChanged(other, ColumnWidthAttribute::unit),
            )
        }

    override fun Layout.computeBoundaries(): LayoutElementBoundaries =
        if (!auto) query.elementBoundaries(width = Width(value = px.toFloat(), UnitsOfMeasure.PX))
        else query.elementBoundaries()

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

}

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.width(block: ColumnWidthAttribute.Builder.() -> Unit) =
    attribute(ColumnWidthAttribute.Builder().apply(block))

fun <T : Any> TableLevelAttributesBuilderApi<T>.columnWidth(block: ColumnWidthAttribute.Builder.() -> Unit) =
    attribute(ColumnWidthAttribute.Builder().apply(block))