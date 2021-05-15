package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle

data class CellBordersAttribute(
    val leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleAttribute<CellBordersAttribute>() {

    class Builder : CellAttributeBuilder<CellBordersAttribute> {
        var leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE
        var leftBorderColor: Color? = null
        var rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE
        var rightBorderColor: Color? = null
        var topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE
        var topBorderColor: Color? = null
        var bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE
        var bottomBorderColor: Color? = null

        override fun build(): CellBordersAttribute = CellBordersAttribute(
            leftBorderStyle,
            leftBorderColor,
            rightBorderStyle,
            rightBorderColor,
            topBorderStyle,
            topBorderColor,
            bottomBorderStyle,
            bottomBorderColor,
        )
    }

    override fun mergeWith(other: CellBordersAttribute): CellBordersAttribute = CellBordersAttribute(
        leftBorderStyle = other.leftBorderStyle ?: this.leftBorderStyle,
        leftBorderColor = other.leftBorderColor ?: this.leftBorderColor,
        rightBorderStyle = other.rightBorderStyle ?: this.rightBorderStyle,
        rightBorderColor = other.rightBorderColor ?: this.rightBorderColor,
        topBorderStyle = other.topBorderStyle ?: this.topBorderStyle,
        topBorderColor = other.topBorderColor ?: this.topBorderColor,
        bottomBorderStyle = other.bottomBorderStyle ?: this.bottomBorderStyle,
        bottomBorderColor = other.bottomBorderColor ?: this.bottomBorderColor,
    )
}

fun <T> CellLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block).build())

fun <T> ColumnLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block).build())

fun <T> RowLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block).build())

fun <T> TableLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block).build())
