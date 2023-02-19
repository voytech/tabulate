package io.github.voytech.tabulate.components.table.api.builder.dsl


import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.ColumnContext
import io.github.voytech.tabulate.components.table.operation.RowContext
import io.github.voytech.tabulate.components.table.operation.TableStart
import io.github.voytech.tabulate.core.model.attributes.*


// Attributes available on table level
fun <T: Any> TableLevelAttributesBuilderApi<T>.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<TableStart>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellContext>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellContext>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellContext>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellContext>().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.columnWidth(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder(ColumnContext::class.java).apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.rowHeight(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder(RowContext::class.java).apply(block))

//Attributes available on column level
fun <T: Any> ColumnLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellContext>().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellContext>().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellContext>().apply(block))

fun <T : Any> ColumnLevelAttributesBuilderApi<T>.width(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder(ColumnContext::class.java).apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellContext>().apply(block))


// Attributes available on row level

fun <T: Any> RowLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellContext>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder(RowContext::class.java).apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellContext>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.rowBorders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder(RowContext::class.java).apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellContext>().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.height(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder(RowContext::class.java).apply(block))


// Attributes available on cell level
fun <T: Any> CellLevelAttributesBuilderApi<T>.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<CellContext>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<CellContext>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<CellContext>().apply(block))

fun <T: Any> CellLevelAttributesBuilderApi<T>.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<CellContext>().apply(block))

