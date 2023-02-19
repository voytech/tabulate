package io.github.voytech.tabulate.components.table.model.attributes.cell.enums

import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellType

enum class DefaultTypeHints: CellType {
    NUMERIC,
    STRING,
    BOOLEAN,
    DATE,
    IMAGE_URI,
    IMAGE_DATA;

    override fun getCellTypeId(): String = name

}