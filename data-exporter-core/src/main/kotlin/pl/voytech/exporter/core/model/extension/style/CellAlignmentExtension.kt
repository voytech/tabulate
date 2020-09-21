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
}

fun alignment(block: CellAlignmentExtension.Builder.() -> Unit): CellExtension = CellAlignmentExtension.Builder().apply(block).build()
