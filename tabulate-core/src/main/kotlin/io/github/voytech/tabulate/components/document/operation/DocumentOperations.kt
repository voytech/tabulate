package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.core.model.ExportApi
import io.github.voytech.tabulate.core.operation.AttributedEntity

sealed class DocumentEntity(scope: ExportApi) : AttributedEntity() {
    init {
        additionalAttributes = scope.getCustomAttributes().data
    }
}

class DocumentStart(scope: ExportApi) : DocumentEntity(scope)

class DocumentEnd(scope: ExportApi) : DocumentEntity(scope)