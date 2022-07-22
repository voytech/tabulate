package io.github.voytech.tabulate.excel.components.table.model

import io.github.voytech.tabulate.core.model.background.FillType

/**
 * Excel format specific cell fill styles
 * @author Wojciech Mąka
 * @since 0.1.0
 */
enum class ExcelCellFills : FillType {
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