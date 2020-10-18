package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute

data class CellBackgroundAttribute(
    val color: Color
) : CellStyleAttribute() {
    class Builder : CellAttributeBuilder {
        lateinit var color: Color
        override fun build(): CellAttribute = CellBackgroundAttribute(color)
    }

    override fun mergeWith(other: CellAttribute): CellAttribute = CellBackgroundAttribute(
        color = if (other is CellBackgroundAttribute) other.color else this.color
    )
}

fun background(block: CellBackgroundAttribute.Builder.() -> Unit): CellAttribute =
    CellBackgroundAttribute.Builder().apply(block).build()
