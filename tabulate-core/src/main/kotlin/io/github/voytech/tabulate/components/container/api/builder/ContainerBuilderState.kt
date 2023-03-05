package io.github.voytech.tabulate.components.container.api.builder

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Orientation

class ContainerBuilderState : CompositeModelBuilderState<Container> {

    @get:JvmSynthetic
    internal var nodes: MutableList<ModelBuilderState<*>> = mutableListOf()

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var orientation: Orientation = Orientation.HORIZONTAL

    @JvmSynthetic
    override fun <E : AbstractModel<E>> bind(node: ModelBuilderState<E>) {
        nodes.add(node)
    }

    @JvmSynthetic
    override fun build(): Container = Container(
        orientation = orientation,
        models = nodes.map { it.build() },
        attributes = null
    )

}