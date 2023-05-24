package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface StartTableOperation<CTX : RenderingContext>: VoidOperation<CTX, TableStartRenderable>

fun interface StartColumnOperation<CTX : RenderingContext>: VoidOperation<CTX, ColumnStartRenderable>

fun interface StartRowOperation<CTX : RenderingContext>: VoidOperation<CTX, RowStartRenderable>

fun interface RenderRowCellOperation<CTX : RenderingContext>: Operation<CTX, CellRenderable>

fun interface EndRowOperation<CTX : RenderingContext, T: Any>: VoidOperation<CTX, RowEndRenderable<T>>

fun interface EndColumnOperation<CTX : RenderingContext>: VoidOperation<CTX, ColumnEndRenderable>

fun interface EndTableOperation<CTX : RenderingContext>: VoidOperation<CTX, TableEndRenderable>