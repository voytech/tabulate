package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.core.operation.CustomAttributesData
import io.github.voytech.tabulate.core.operation.AxisBoundStatus

sealed interface ContextResult<CTX: CustomAttributesData>

data class SuccessResult<CTX: CustomAttributesData>(internal val context: CTX): ContextResult<CTX>

data class OverflowResult<CTX: CustomAttributesData>(internal val overflow: AxisBoundStatus): ContextResult<CTX>
