package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.BoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface StartTableOperation<CTX : RenderingContext> : VoidOperation<CTX, TableStartRenderableEntity>

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class TableContext(
    attributes: Attributes?,
) : RenderableEntity<TableLayout>(attributes) {

    override val boundaryToFit = BoundaryType.BORDER

    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getBorderRectangle().leftTop.x,
            y = getBorderRectangle().leftTop.y,
            // provide width+height only on RENDERING phase when everything is measured and calculated
            // As long as defineBoundingBox is also called in MEASURING phase we cannot assume width+height are values to rely on.
            // TODO - consider diverging defineBoundingBox into defineMeasuringBoundingBox and defineRenderingBoundingBox or something similar
            width = whenMeasured { getBorderRectangle().getWidth() },
            height = whenMeasured { getBorderRectangle().getHeight() },
            boundaryToFit
        )
}

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableStartRenderableEntity(
    attributes: Attributes?,
) : TableContext(attributes) {
    override fun toString(): String {
        return "TableStartRenderable(attributes=$attributes)"
    }
}

internal fun <T : Any> Table<T>.asTableStart(customAttributes: StateAttributes): TableStartRenderableEntity =
    TableStartRenderableEntity(attributes?.forContext<TableStartRenderableEntity>()).apply {
        additionalAttributes = customAttributes.data
    }