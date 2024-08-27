package io.github.voytech.tabulate.components.table.model.attributes.cell

import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellType
import io.github.voytech.tabulate.components.table.rendering.CellRenderableEntity
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute

data class TypeHintAttribute(
    val type: CellType,
) : Attribute<TypeHintAttribute>() {
    @TabulateMarker
    class Builder : AttributeBuilder<TypeHintAttribute>(CellRenderableEntity::class.java) {
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

fun <T: Any> CellLevelAttributesBuilderApi<T>.cellType(block: () -> CellType) =
    attribute(TypeHintAttribute.Builder().apply { type = block() })

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.cellType(block: () -> CellType) =
    attribute(TypeHintAttribute.Builder().apply { type = block() })
