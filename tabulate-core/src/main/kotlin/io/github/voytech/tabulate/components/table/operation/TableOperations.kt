package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.Operation

fun interface StartTableOperation<CTX : RenderingContext>: Operation<CTX, TableAttribute<*>, TableStart>

fun interface StartColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnAttribute<*>, ColumnStart>

fun interface StartRowOperation<CTX : RenderingContext>: Operation<CTX, RowAttribute<*>, RowStart>

fun interface RenderRowCellOperation<CTX : RenderingContext>: Operation<CTX, CellAttribute<*>, CellContext>

fun interface EndRowOperation<CTX : RenderingContext>: Operation<CTX, RowAttribute<*>, RowEnd<*>>

fun interface EndColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnAttribute<*>, ColumnEnd>

fun interface EndTableOperation<CTX : RenderingContext>: Operation<CTX, TableAttribute<*>, TableEnd>