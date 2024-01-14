package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.core.model.ExecutionContext

data class PageExecutionContext(
    var pageNumber: Int = 0,
    var pageTitle: String? = null,
    var pageDescription: String? = null
): ExecutionContext {
    fun currentPageTitleWithNumber(): String = "$pageTitle ($pageNumber)"
}