package io.github.voytech.tabulate.components.text.model

import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.*

class Text(
    @get:JvmSynthetic
    internal val value: String = "blank",
    private val valueSupplier: ReifiedValueSupplier<*, String>?,
    override val attributes: Attributes?,
) : DirectlyRenderableModel<TextRenderable>() {

    override fun ExportApi.asRenderable(): TextRenderable =
        TextRenderable(getTextValue(), attributes.orEmpty().forContext<TextRenderable>(), getCustomAttributes())

    private fun ExportApi.getTextValue(): String = valueSupplier?.let { value(it) } ?: value

}