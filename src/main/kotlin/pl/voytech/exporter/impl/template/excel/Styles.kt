package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.model.hints.CellHint

data class CellExcelDataFormatHint(
   val dataFormat: String
): CellHint()