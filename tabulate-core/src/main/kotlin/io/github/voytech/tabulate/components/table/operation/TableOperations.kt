package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.Operation

fun interface StartTableOperation<CTX : RenderingContext>: Operation<CTX, TableStart>

fun interface StartColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnStart>

fun interface StartRowOperation<CTX : RenderingContext>: Operation<CTX, RowStart>

fun interface RenderRowCellOperation<CTX : RenderingContext>: Operation<CTX, CellContext>

fun interface EndRowOperation<CTX : RenderingContext, T: Any>: Operation<CTX, RowEnd<T>>

fun interface EndColumnOperation<CTX : RenderingContext>: Operation<CTX, ColumnEnd>

fun interface EndTableOperation<CTX : RenderingContext>: Operation<CTX, TableEnd>