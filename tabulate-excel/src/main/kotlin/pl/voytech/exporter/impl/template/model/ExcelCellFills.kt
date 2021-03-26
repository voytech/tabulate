package pl.voytech.exporter.impl.template.model

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.CellFill

enum class ExcelCellFills : CellFill {
    LEAST_DOTS,
    LESS_DOTS,
    SPARSE_DOTS,
    THICK_BACKWARD_DIAG,
    THICK_FORWARD_DIAG,
    THICK_HORZ_BANDS,
    THICK_VERT_BANDS,
    THIN_BACKWARD_DIAG,
    THIN_FORWARD_DIAG,
    THIN_HORZ_BANDS,
    THIN_VERT_BANDS;

    override fun getCellFillId() = name
}