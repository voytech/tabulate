package io.github.voytech.tabulate.excel.model

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellType

enum class ExcelTypeHints : CellType {
    NUMERIC,
    STRING,
    BOOLEAN,
    DATE,
    FORMULA,
    ERROR,
    IMAGE_URL,
    IMAGE_DATA;
    override fun getCellTypeId(): String = name

}