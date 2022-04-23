package io.github.voytech.tabulate.components.table.model.attributes.cell

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellType
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

data class TypeHintAttribute(
    val type: CellType,
) : CellAttribute<TypeHintAttribute>() {
    @TabulateMarker
    class Builder : CellAttributeBuilder<TypeHintAttribute>() {
        var type: CellType by observable(DefaultTypeHints.STRING)
        override fun provide(): TypeHintAttribute = TypeHintAttribute(type)
    }

    override fun overrideWith(other: TypeHintAttribute): TypeHintAttribute = TypeHintAttribute(
        type = takeIfChanged(other, TypeHintAttribute::type)
    )

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}

fun <T> CellLevelAttributesBuilderApi<T>.cellType(block: () -> CellType) =
    attribute(TypeHintAttribute.Builder().apply { type = block() })

fun <T> ColumnLevelAttributesBuilderApi<T>.cellType(block: () -> CellType) =
    attribute(TypeHintAttribute.Builder().apply { type = block() })
