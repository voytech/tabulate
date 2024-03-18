package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface EndColumnOperation<CTX : RenderingContext>: VoidOperation<CTX, ColumnEndRenderable>

class ColumnEndRenderable(
    attributes: Attributes? = null,
    val columnIndex: Int,
) :  ColumnRenderable(attributes), LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout>, ColumnCoordinate {
    override fun getColumn(): Int = columnIndex

    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(0),
            width = getMeasuredColumnWidth(getColumn(),1),
            height = getCurrentContentSize().height,
            boundaryToFit
        )
    }

    override fun LayoutSpace.applyBoundingBox(bbox: RenderableBoundingBox, layout: TableLayout) {
        with(layout) {
            bbox.width?.let {
                val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<WidthAttribute>() } ?: SizingOptions.SET
                setColumnWidth(getColumn(), it, ops)
            }
        }
    }

}

internal fun <T : Any> ColumnDef<T>.asColumnEnd(
    table: Table<T>,
    attributes: Attributes,
    customAttributes: StateAttributes,
) = ColumnEndRenderable(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes.data }