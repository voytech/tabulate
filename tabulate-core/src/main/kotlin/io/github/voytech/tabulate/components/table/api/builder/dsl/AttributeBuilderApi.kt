package io.github.voytech.tabulate.components.table.api.builder.dsl


import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.CellRenderable
import io.github.voytech.tabulate.components.table.operation.ColumnRenderable
import io.github.voytech.tabulate.components.table.operation.RowRenderable
import io.github.voytech.tabulate.components.table.operation.TableStartRenderable
import io.github.voytech.tabulate.core.model.attributes.*


// Attributes available on table level
fun <T: Any> TableLevelAttributesBuilderApi<T>.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<Table<*>>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.tableBorders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<TableStartRenderable>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.columnWidth(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder(ColumnRenderable::class.java).apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.rowHeight(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder(RowRenderable::class.java).apply(block))

//Attributes available on column level
fun <T: Any> ColumnLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellRenderable>().apply(block))

fun <T : Any> ColumnLevelAttributesBuilderApi<T>.width(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder(ColumnRenderable::class.java).apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellRenderable>().apply(block))


// Attributes available on row level

fun <T: Any> RowLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder(RowRenderable::class.java).apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.rowBorders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder(RowRenderable::class.java).apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.height(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder(RowRenderable::class.java).apply(block))


// Attributes available on cell level
fun <T: Any> CellLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellRenderable>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellRenderable>().apply(block))

