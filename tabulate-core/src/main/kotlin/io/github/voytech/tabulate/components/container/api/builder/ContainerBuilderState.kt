package io.github.voytech.tabulate.components.container.api.builder

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.core.api.builder.AttributesAwareBuilder
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.DescendantsIterationsKind
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.reify
import java.util.*

class ContainerBuilderState : CompositeModelBuilderState<Container>, AttributesAwareBuilder<Container>() {

    @JvmSynthetic
    internal var forcePreMeasure: Boolean = false

    @JvmSynthetic
    internal var id: String = UUID.randomUUID().toString()

    @JvmSynthetic
    internal var nodes: MutableList<ModelBuilderState<*>> = mutableListOf()

    @JvmSynthetic
    internal var orientation: Orientation = Orientation.HORIZONTAL

    @JvmSynthetic
    internal var descendantsIterationsKind: DescendantsIterationsKind = DescendantsIterationsKind.POSTPONED

    @JvmSynthetic
    override fun <E : AbstractModel> bind(node: ModelBuilderState<E>) {
        nodes.add(node)
    }

    @JvmSynthetic
    override fun build(): Container = Container(
        id = id,
        forcePreMeasure = forcePreMeasure,
        attributes = attributes(),
        orientation = orientation,
        descendantsIterationsKind = descendantsIterationsKind,
        models = nodes.map { it.build() }
    )

    override fun modelClass(): Class<Container> = reify()

}