package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.api.builder.CellExtensionBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment

data class CellAlignmentExtension(
    val vertical: VerticalAlignment? = null,
    val horizontal: HorizontalAlignment? = null
) : CellStyleExtension() {
    class Builder : CellExtensionBuilder {
        var vertical: VerticalAlignment? = null
        var horizontal: HorizontalAlignment? = null
        override fun build(): CellExtension = CellAlignmentExtension(vertical, horizontal)
    }

    override fun mergeWith(other: CellExtension): CellExtension = CellAlignmentExtension(
        vertical = if (other is CellAlignmentExtension) other.vertical ?: this.vertical else this.vertical,
        horizontal = if (other is CellAlignmentExtension) other.horizontal ?: this.horizontal else this.horizontal
    )

}

fun alignment(block: CellAlignmentExtension.Builder.() -> Unit): CellExtension =
    CellAlignmentExtension.Builder().apply(block).build()
