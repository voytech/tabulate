package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder

data class CellBackgroundAttribute(
    val color: Color
) : CellStyleAttribute<CellBackgroundAttribute>() {
    class Builder : CellAttributeBuilder<CellBackgroundAttribute> {
        lateinit var color: Color
        override fun build(): CellBackgroundAttribute = CellBackgroundAttribute(color)
    }

    override fun mergeWith(other: CellBackgroundAttribute): CellBackgroundAttribute = CellBackgroundAttribute(
        color = other.color
    )
}

fun background(block: CellBackgroundAttribute.Builder.() -> Unit): CellBackgroundAttribute =
    CellBackgroundAttribute.Builder().apply(block).build()
