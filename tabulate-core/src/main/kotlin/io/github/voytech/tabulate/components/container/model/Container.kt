package io.github.voytech.tabulate.components.container.model

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutConstraints
import io.github.voytech.tabulate.core.template.layout.policy.FlowLayoutPolicy

class Container(
    override val attributes: Attributes?,
    @get:JvmSynthetic
    internal val orientation: Orientation = Orientation.VERTICAL,
    @get:JvmSynthetic
    internal val enableFold: Boolean = true, // when enabled, rendering goes to next line, if disabled - group is overflowed and suspended.
    @get:JvmSynthetic
    internal val models: List<AbstractModel<*>> = emptyList(),
    override val policy: FlowLayoutPolicy = FlowLayoutPolicy(),
) : ModelWithAttributes<Container>(), LayoutPolicyProvider<FlowLayoutPolicy> {

    lateinit var position: Position

    private fun Layout.currentPosition(): LayoutConstraints =
        LayoutConstraints(position, maxRightBottom, orientation)

    override fun doExport(exportContext: ModelExportContext) = with(exportContext) {
        currentLayout().run {
            position = leftTop
            models.forEach { model ->
                exportAndResumeIfNeeded(model, exportContext)
            }
        }
    }

    private fun Layout.exportAndResumeIfNeeded(model: AbstractModel<*>, exportContext: ModelExportContext) {
        val size = model.measure(exportContext)
        val status = model.exportWithStatus(exportContext, currentPosition())
        if (status.isXOverflow() && orientation == Orientation.HORIZONTAL) {
            position = with(boundingRectangle) { Position(leftTop.x, rightBottom.y) }
            model.export(exportContext, currentPosition())
        }
        val mdlLayoutX = model.getPosition()?.x ?: X.zero(uom)
        position = Position(mdlLayoutX + size.width.orZero(uom), position.y)
    }

}