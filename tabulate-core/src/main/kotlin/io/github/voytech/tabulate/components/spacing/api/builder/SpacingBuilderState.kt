package io.github.voytech.tabulate.components.spacing.api.builder

import io.github.voytech.tabulate.components.spacing.model.Spacing
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.*
import java.util.*


/**
 * Top level builder state for creating spacing model.
 * Manages mutable state that is eventually materialized to spacing.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class SpacingBuilderState : CompositeModelBuilderState<Spacing> {

    @get:JvmSynthetic
    internal var child: ModelBuilderState<*>? = null

    @get:JvmSynthetic
    var widthInPoints: Float = 0f

    @get:JvmSynthetic
    var heightInPoints: Float = 0f

    @JvmSynthetic
    override fun <E: Model<E>> bind(node: ModelBuilderState<E>) {
        child = node
    }

    @JvmSynthetic
    override fun build(): Spacing = Spacing(
        child = child?.build(),
        id = UUID.randomUUID().toString(),
        size = Size(Width(widthInPoints, UnitsOfMeasure.PT), Height(heightInPoints, UnitsOfMeasure.PT)),
        childOrientation = Orientation.HORIZONTAL
    )

}
