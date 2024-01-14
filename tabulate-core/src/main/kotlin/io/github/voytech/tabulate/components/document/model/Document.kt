package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.core.model.*

class Document internal constructor(
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel>,
    @get:JvmSynthetic override val attributes: Attributes?,
    override val id: String,
) : ModelWithAttributes() {

    override fun doExport(api: ExportApi) = api {
        render(DocumentStart(api))
        nodes.forEach { child ->
            do {
                child.export()
            } while (api.continuations().haveChildrenPendingIterations())
        }
        render(DocumentEnd(api))
    }

}