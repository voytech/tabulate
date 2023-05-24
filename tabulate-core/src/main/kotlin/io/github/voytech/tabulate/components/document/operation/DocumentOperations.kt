package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.core.model.ExportApi
import io.github.voytech.tabulate.core.operation.AttributedContext

sealed class DocumentContext(scope: ExportApi) : AttributedContext() {
    init {
        additionalAttributes = scope.getCustomAttributes().data
    }
}

class DocumentStart(scope: ExportApi) : DocumentContext(scope)

class DocumentEnd(scope: ExportApi) : DocumentContext(scope)