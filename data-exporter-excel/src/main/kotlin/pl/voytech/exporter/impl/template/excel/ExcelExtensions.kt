package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.model.extension.CellExtension

data class CellExcelDataFormatExtension(
    val dataFormat: String
) : CellExtension() {
    override fun mergeWith(other: CellExtension): CellExtension  = CellExcelDataFormatExtension(
        dataFormat = if (other is CellExcelDataFormatExtension) other.dataFormat else this.dataFormat
    )
}