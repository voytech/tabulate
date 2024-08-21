package io.github.voytech.tabulate.components.text.api.builder

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.core.api.builder.*
import io.github.voytech.tabulate.core.model.ReifiedValueSupplier
import io.github.voytech.tabulate.core.model.ValueSupplier
import io.github.voytech.tabulate.core.reify
import java.util.*


/**
 * Builder state for creating text model.
 * Manages mutable state that is eventually materialized to text model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class TextBuilderState : ModelBuilderState<Text>, AttributesAwareBuilder<Text>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var id: String = UUID.randomUUID().toString()

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var value: String = "?"

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var valueSupplier: ReifiedValueSupplier<*,String>? = null

    @JvmSynthetic
    override fun build(): Text = Text(id = id, value= value, valueSupplier = valueSupplier, attributes = attributes())

    override fun modelClass(): Class<Text> = reify()

}
