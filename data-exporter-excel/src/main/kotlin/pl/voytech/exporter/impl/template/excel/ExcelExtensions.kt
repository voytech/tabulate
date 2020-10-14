package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.api.builder.CellExtensionBuilder
import pl.voytech.exporter.core.model.extension.CellExtension

data class CellExcelDataFormatExtension(
    val dataFormat: String
) : CellExtension() {
    class Builder : CellExtensionBuilder {
        var value: String = "general"
        override fun build(): CellExtension = CellExcelDataFormatExtension(value)
    }
    override fun mergeWith(other: CellExtension): CellExtension  = CellExcelDataFormatExtension(
        dataFormat = if (other is CellExcelDataFormatExtension) other.dataFormat else this.dataFormat
    )
}

fun dataFormat(block: CellExcelDataFormatExtension.Builder.() -> Unit): CellExtension = CellExcelDataFormatExtension.Builder().apply(block).build()
