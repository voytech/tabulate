package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.api.builder.ColumnExtensionBuilder
import pl.voytech.exporter.core.model.extension.ColumnExtension

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthExtension(
    val auto: Boolean? = false,
    val width: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL
) : ColumnExtension() {
    override fun beforeFirstRow() = true
    override fun afterLastRow() = true

    class Builder : ColumnExtensionBuilder {
        var auto: Boolean? = false
        var width: Int = -1
        var unit: LengthUnit = LengthUnit.PIXEL
        override fun build(): ColumnExtension = ColumnWidthExtension(auto, width, unit)
    }
}

fun size(block: ColumnWidthExtension.Builder.() -> Unit): ColumnExtension = ColumnWidthExtension.Builder().apply(block).build()

