package io.github.voytech.tabulate.components.image.api.builder

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.core.api.builder.AttributesAwareBuilder
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.reify
import java.util.*


/**
 * Builder state for creating text model.
 * Manages mutable state that is eventually materialized to text model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class ImageBuilderState : ModelBuilderState<Image>, AttributesAwareBuilder<Image>() {

    @JvmSynthetic
    internal var id: String = UUID.randomUUID().toString()

    @JvmSynthetic
    internal var filePath: String = "?"

    @JvmSynthetic
    override fun build(): Image = Image(id,filePath, attributes())

    override fun modelClass(): Class<Image> = reify()

}
