package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.BoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.operation.*

fun interface EndRowOperation<CTX : RenderingContext, T : Any> : VoidOperation<CTX, RowEndRenderableEntity<T>>

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally, it contains also all resolved cell operation context for each contained cell.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowEndRenderableEntity<T>(
    attributes: Attributes?,
    val rowCellValues: Map<ColumnKey<T>, CellRenderableEntity>,
    val rowIndex: Int,
) : RowRenderableEntity(attributes), RowLayoutElement {

    override fun getRow(): Int = rowIndex

    override val boundaryToFit: BoundaryType = BoundaryType.CONTENT

    fun getCells(): Map<ColumnKey<T>, CellRenderableEntity> = rowCellValues

    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = whileMeasuring { getUnconfirmedRowWidth(getRow()) } ?: getCurrentContentSize().width,
            height = whileMeasuring { getUnconfirmedRowHeight(getRow()) } ?: getCurrentRowHeight(getRow(), 1, uom),
            boundaryToFit
        )

    override fun TableLayout.applyMeasures(bbox: RenderableBoundingBox, status: RenderingStatus) {
        whileMeasuring {
            if (status.hasLayoutEffect()) {
                if (status.isClipped()) {
                    shrinkProposedRowToFit(getRow(), bbox.size)
                }
                confirmProposedRowSize(getRow())
            } else {
                rollbackProposedRowSize(getRow())
            }
        }
    }

    override fun toString(): String {
        return "RowEndRenderable(rowIndex=$rowIndex)"
    }
}

internal fun <T : Any> SyntheticRow<T>.createRowEnd(
    rowStart: RowStartRenderableEntity,
    rowCellValues: Map<ColumnKey<T>, CellRenderableEntity>,
): RowEndRenderableEntity<T> =
    RowEndRenderableEntity(
        rowIndex = rowStart.rowIndex,
        attributes = rowEndAttributes,
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = rowStart.additionalAttributes }