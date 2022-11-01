package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.core.model.ExecutionContext

data class PageExecutionContext(
    var pageNumber: Int = 0,
    var description: String? = null
): ExecutionContext