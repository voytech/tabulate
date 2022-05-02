package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.Operation

fun interface OpenTableOperation<CTX : RenderingContext>: Operation<CTX, TableAttribute<*>, TableStart>

fun interface OpenColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnAttribute<*>, ColumnStart>

fun interface OpenRowOperation<CTX : RenderingContext>: Operation<CTX, RowAttribute<*>, RowStart>

fun interface RenderRowCellOperation<CTX : RenderingContext>: Operation<CTX, CellAttribute<*>, CellContext>

fun interface CloseRowOperation<CTX : RenderingContext>: Operation<CTX, RowAttribute<*>, RowEnd<*>>

fun interface CloseColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnAttribute<*>, ColumnEnd>

fun interface CloseTableOperation<CTX : RenderingContext>: Operation<CTX, TableAttribute<*>, TableEnd>