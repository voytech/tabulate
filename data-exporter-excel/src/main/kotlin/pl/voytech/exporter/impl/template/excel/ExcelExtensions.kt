package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.model.extension.CellExtension

data class CellExcelDataFormatExtension(
    val dataFormat: String
) : CellExtension()