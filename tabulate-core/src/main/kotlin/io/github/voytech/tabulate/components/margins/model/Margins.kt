package io.github.voytech.tabulate.components.margins.model

import io.github.voytech.tabulate.components.margins.template.MarginsTemplate
import io.github.voytech.tabulate.components.margins.template.MarginsTemplateContext
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.Size

class Margins internal constructor(
    @get:JvmSynthetic
    internal val size: Size,
    @get:JvmSynthetic
    internal val childOrientation: Orientation,
    @get:JvmSynthetic
    internal val child: AbstractModel<*,*,*>? = null,
) : AbstractModel<MarginsTemplate,Margins,MarginsTemplateContext>() {
    override fun getExportTemplate() = MarginsTemplate()
}

