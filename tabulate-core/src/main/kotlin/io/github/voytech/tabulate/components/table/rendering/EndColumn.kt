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
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.operation.hasLayoutEffect

fun interface EndColumnOperation<CTX : RenderingContext> : VoidOperation<CTX, ColumnEndRenderableEntity>

class ColumnEndRenderableEntity(
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnRenderableEntity(attributes), LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout>, ColumnCoordinate {
    override fun getColumn(): Int = columnIndex

    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(0),
            width = getMeasuredColumnWidth(getColumn(), 1),
            height = getCurrentContentSize().height,
            boundaryToFit
        )


    override fun TableLayout.absorbRenderableBoundingBox(bbox: RenderableBoundingBox, status: RenderingStatus) {
        if (!status.hasLayoutEffect()) return
        bbox.width?.let {
            val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<WidthAttribute>() } ?: SizingOptions.SET
            setColumnWidth(getColumn(), it, ops)
        }
    }

    override fun toString(): String {
        return "ColumnEndRenderable(columnIndex=$columnIndex)"
    }

}

internal fun <T : Any> ColumnDef<T>.asColumnEnd(
    table: Table<T>,
    attributes: Attributes,
    customAttributes: StateAttributes,
) = ColumnEndRenderableEntity(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes.data }