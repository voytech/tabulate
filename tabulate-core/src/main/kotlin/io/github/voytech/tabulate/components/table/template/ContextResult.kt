package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.core.template.operation.ContextData
import io.github.voytech.tabulate.core.template.operation.OverflowResult

sealed interface ContextResult<CTX: ContextData>

data class SuccessResult<CTX: ContextData>(internal val context: CTX): ContextResult<CTX>

data class OverflowResult<CTX: ContextData>(internal val overflow: OverflowResult): ContextResult<CTX>
