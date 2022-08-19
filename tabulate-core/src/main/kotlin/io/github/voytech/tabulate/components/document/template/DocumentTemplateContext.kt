package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext
import java.util.*

class DocumentTemplateContext(
    document: Document,
    services: ExportTemplateServices
) : TemplateContext<DocumentTemplateContext,Document>(document, mutableMapOf(),services) {
    init {
        stateAttributes["_exportTraceId"] = UUID.randomUUID().toString()
    }
}
