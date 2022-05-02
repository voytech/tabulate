package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.TemplateContext

interface Model<M: Model<M>> {
    fun getId(): String

    fun getExportTemplate(): ExportTemplate<M,out TemplateContext<M>>? = null

}