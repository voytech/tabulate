package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellType

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
