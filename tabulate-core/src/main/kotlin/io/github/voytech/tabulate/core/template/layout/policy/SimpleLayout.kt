package io.github.voytech.tabulate.core.template.layout.policy

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Overflow

class SimpleLayoutPolicy : LayoutPolicy {

    override var isSpaceMeasured: Boolean = false

    override fun Layout.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    override fun Layout.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X(absoluteX.value + relativeX.value, targetUnit)
    }

    override fun Layout.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y(absoluteY.value + relativeY.value, targetUnit)
    }

    /**
     * Extend layout rendered space by specific [Width].
     */
    override fun Layout.extend(width: Width) {
        extend(width)
    }

    /**
     * Extend layout rendered space by specific [Height].
     */
    override fun Layout.extend(height: Height) {
        extend(height)
    }

    override fun ModelExportContext.setOverflow(overflow: Overflow) = when (overflow) {
        Overflow.X -> suspendX()
        Overflow.Y -> suspendY()
    }

}
