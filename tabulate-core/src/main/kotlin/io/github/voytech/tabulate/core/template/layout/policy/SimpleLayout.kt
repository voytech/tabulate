package io.github.voytech.tabulate.core.template.layout.policy

import io.github.voytech.tabulate.core.model.ExportStatus
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.template.layout.AbstractLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Overflow

class SimpleLayoutPolicy : AbstractLayoutPolicy() {

    override fun ModelExportContext.overflow(overflow: Overflow) = when (overflow) {
        Overflow.X -> { status = ExportStatus.OVERFLOWED }
        Overflow.Y -> { status = ExportStatus.OVERFLOWED }
    }

}
