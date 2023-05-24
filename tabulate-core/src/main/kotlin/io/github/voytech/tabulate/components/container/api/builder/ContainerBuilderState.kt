package io.github.voytech.tabulate.components.container.api.builder

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.core.api.builder.AttributesAwareBuilder
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.reify

class ContainerBuilderState : CompositeModelBuilderState<Container>, AttributesAwareBuilder<Container>() {

    @get:JvmSynthetic
    internal var nodes: MutableList<ModelBuilderState<*>> = mutableListOf()

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var orientation: Orientation = Orientation.HORIZONTAL

    @JvmSynthetic
    override fun <E : AbstractModel> bind(node: ModelBuilderState<E>) {
        nodes.add(node)
    }

    @JvmSynthetic
    override fun build(): Container = Container(
        attributes = attributes(),
        orientation = orientation,
        models = nodes.map { it.build() }
    )

    override fun modelClass(): Class<Container> = reify()

}