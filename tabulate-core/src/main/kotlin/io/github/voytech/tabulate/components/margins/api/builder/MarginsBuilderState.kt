package io.github.voytech.tabulate.components.margins.api.builder

import io.github.voytech.tabulate.components.margins.model.Margins
import io.github.voytech.tabulate.core.api.builder.BuiltModel
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.*


/**
 * Top level builder state for creating spacing model.
 * Manages mutable state that is eventually materialized to spacing.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class MarginsBuilderState : CompositeModelBuilderState<Margins> {

    @get:JvmSynthetic
    internal var child: ModelBuilderState<*>? = null

    @get:JvmSynthetic
    var width: Width = Width.zero(UnitsOfMeasure.PT)

    @get:JvmSynthetic
    var height: Height = Height.zero(UnitsOfMeasure.PT)

    @JvmSynthetic
    override fun <E : BuiltModel<E>> bind(node: ModelBuilderState<E>) {
        child = node
    }

    @JvmSynthetic
    override fun build(): Margins = Margins(
        child = child?.build(),
        size = Size(width, height),
        childOrientation = Orientation.HORIZONTAL
    )

}
