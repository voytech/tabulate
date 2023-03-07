package io.github.voytech.tabulate.components.container.model

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.Layout

class Container(
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val orientation: Orientation = Orientation.VERTICAL,
    @get:JvmSynthetic
    internal val enableFold: Boolean = true, // when enabled, rendering goes to next line, if disabled - group is overflowed and suspended.
    @get:JvmSynthetic
    internal val models: List<AbstractModel<*>> = emptyList(),
) : ModelWithAttributes<Container>() {

    lateinit var position: Position

    private fun Layout.currentPosition(): LayoutConstraints =
        LayoutConstraints(position, maxRightBottom, orientation)

    override fun doExport(exportContext: ModelExportContext) {
        createLayoutScope(orientation) {
            position = leftTop
            models.forEach { model ->
                exportAndResumeIfNeeded(model, exportContext)
            }
        }
    }

    private fun Layout.exportAndResumeIfNeeded(model: AbstractModel<*>, exportContext: ModelExportContext) {
        val size = model.measure(exportContext)
        val status = model.exportWithStatus(exportContext,currentPosition())
        // Should call loop in here in order to resume suspended model straight away if space available.
        if (status.isXOverflow() && orientation == Orientation.HORIZONTAL) {
            position = with(boundingRectangle) { Position(leftTop.x, rightBottom.y) }
            model.resume(currentPosition())
        }
        val mdlLayoutX = model.getPosition()?.x ?: X.zero(uom)
        position = Position(mdlLayoutX + size.width.orZero(uom), position.y)
    }

}