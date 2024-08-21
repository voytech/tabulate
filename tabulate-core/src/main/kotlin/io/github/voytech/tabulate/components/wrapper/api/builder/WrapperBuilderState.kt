package io.github.voytech.tabulate.components.wrapper.api.builder

import io.github.voytech.tabulate.components.wrapper.model.Wrapper
import io.github.voytech.tabulate.core.api.builder.AttributesAwareBuilder
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.reify
import java.util.*

class WrapperBuilderState : CompositeModelBuilderState<Wrapper>, AttributesAwareBuilder<Wrapper>() {

    @get:JvmSynthetic
    internal var id: String = UUID.randomUUID().toString()

    @get:JvmSynthetic
    internal lateinit var model: ModelBuilderState<*>
    @JvmSynthetic
    override fun <E : AbstractModel> bind(node: ModelBuilderState<E>) {
        model = node
    }

    @JvmSynthetic
    override fun build(): Wrapper = Wrapper(
        id = id,
        attributes = attributes(),
        child = model.build()
    )

    override fun modelClass(): Class<Wrapper> = reify()

}