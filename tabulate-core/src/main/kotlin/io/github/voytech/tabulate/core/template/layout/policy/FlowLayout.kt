package io.github.voytech.tabulate.core.template.layout.policy

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.*

class FlowLayoutPolicy : AbstractLayoutPolicy(), SizeTrackingIteratorPolicyMethods {

    private data class PositionAndSize(var position: Position, var width: Width? = null, var height: Height? = null)

    private val items: MutableList<PositionAndSize> = mutableListOf()

    private var currentIndex = 0

    private var xMax = X.zero()

    private var yMax = Y.zero()

    private lateinit var orientation: Orientation

    private lateinit var uom: UnitsOfMeasure

    override var isSpaceMeasured: Boolean = false

    override fun setCurrentWidth(width: Width) = measuring {
        items[currentIndex].width = width
        (items[currentIndex].position.x + width).let { maybeXMax ->
            if (maybeXMax > xMax) xMax = maybeXMax
        }
    }

    override fun setCurrentHeight(height: Height) = measuring {
        items[currentIndex].height = height
        (items[currentIndex].position.y + height).let { maybeYMax ->
            if (maybeYMax > yMax) yMax = maybeYMax
        }
    }

    override fun getCurrentWidth(uom: UnitsOfMeasure): Width? =
        if (currentIndex <= items.size - 1) {
            items[currentIndex].width?.switchUnitOfMeasure(uom)
        } else null


    override fun getCurrentHeight(uom: UnitsOfMeasure): Height? =
        if (currentIndex <= items.size - 1) {
            items[currentIndex].height?.switchUnitOfMeasure(uom)
        } else null

    override fun next() = measuring {
        items[currentIndex].run {
            width?.let { _width ->
                height?.let { _height ->
                    when (orientation) {
                        Orientation.HORIZONTAL -> PositionAndSize(position.copy(x = position.x + _width))
                        Orientation.VERTICAL -> PositionAndSize(position.copy(y = position.y + _height))
                    }
                }
            }
        }?.let {
            items += it
        }
    }.also { currentIndex++ }

    override fun begin(orientation: Orientation, uom: UnitsOfMeasure) = measuring {
        this.orientation = orientation
        this.uom = uom
        items.clear()
        items += PositionAndSize(Position.start(uom))
    }.also { currentIndex = 0 }

    override fun Layout.getCurrentX(): X = leftTop.x + items[currentIndex].position.x

    override fun Layout.getCurrentY(): Y = leftTop.y + items[currentIndex].position.y

    override fun ModelExportContext.overflow(overflow: Overflow) {
        when (overflow) {
            Overflow.X -> when (orientation) {
                Orientation.HORIZONTAL -> Unit
                Orientation.VERTICAL -> {
                     status = ExportStatus.OVERFLOWED
                }
            }

            Overflow.Y -> when (orientation) {
                Orientation.HORIZONTAL -> {
                    status = ExportStatus.OVERFLOWED
                }
                Orientation.VERTICAL -> Unit
            }
        }
    }

    override fun Layout.setMeasured() {
        isSpaceMeasured = true
        extend(Position(getX(xMax, uom), getY(yMax, uom)))
    }
}