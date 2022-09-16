package io.github.voytech.tabulate.components.text.api.builder

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.core.api.builder.*
import io.github.voytech.tabulate.core.reify


/**
 * Builder state for creating text model.
 * Manages mutable state that is eventually materialized to text model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class TextBuilderState : ModelBuilderState<Text>, AttributesAwareBuilder<Text>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var value: String = "untitled document"

    @JvmSynthetic
    override fun build(): Text = Text(value, attributes())

    override fun modelClass(): Class<Text> = reify()

}
