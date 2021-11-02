package io.github.voytech.tabulate.model.attributes.cell.enums

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellType

enum class DefaultTypeHints: CellType {
    NUMERIC,
    STRING,
    BOOLEAN,
    DATE,
    IMAGE_URL,
    IMAGE_DATA;

    override fun getCellTypeId(): String = name

}