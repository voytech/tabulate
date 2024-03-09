package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.core.operation.ContextData
import io.github.voytech.tabulate.core.operation.AxisBoundStatus

sealed interface ContextResult<CTX: ContextData>

data class SuccessResult<CTX: ContextData>(internal val context: CTX): ContextResult<CTX>

data class OverflowResult<CTX: ContextData>(internal val overflow: AxisBoundStatus): ContextResult<CTX>
