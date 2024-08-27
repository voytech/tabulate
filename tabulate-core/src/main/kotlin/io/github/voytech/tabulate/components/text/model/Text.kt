package io.github.voytech.tabulate.components.text.model

import io.github.voytech.tabulate.components.text.operation.TextRenderableEntity
import io.github.voytech.tabulate.core.model.*
import java.util.*

class Text(
    override val id: String = UUID.randomUUID().toString(),
    @get:JvmSynthetic
    internal val value: String = "blank",
    private val valueSupplier: ReifiedValueSupplier<*, String>?,
    override val attributes: Attributes?,
) : DirectlyRenderableModel<TextRenderableEntity>() {

    override fun ExportApi.asRenderable(): TextRenderableEntity =
        TextRenderableEntity(getTextValue(), attributes.ensure().forContext<TextRenderableEntity>(), getCustomAttributes())

    private fun ExportApi.getTextValue(): String = valueSupplier?.let { value(it) } ?: value

}