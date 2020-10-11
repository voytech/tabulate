package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.api.builder.CellExtensionBuilder
import pl.voytech.exporter.core.model.extension.CellExtension

data class CellBackgroundExtension(
    val color: Color
) : CellStyleExtension() {
    class Builder : CellExtensionBuilder {
        lateinit var color: Color
        override fun build(): CellExtension = CellBackgroundExtension(color)
    }

    override fun mergeWith(other: CellExtension): CellExtension = CellBackgroundExtension(
        color = if (other is CellBackgroundExtension) other.color else this.color
    )
}

fun background(block: CellBackgroundExtension.Builder.() -> Unit): CellExtension =
    CellBackgroundExtension.Builder().apply(block).build()
