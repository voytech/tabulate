package io.github.voytech.tabulate.core.model.clip

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

enum class ClippingMode  {
    CLIP,
    SKIP;
}

interface TextClippingModeBuilder {
    var mode: ClippingMode
}

interface TextClippingModeWords : TextClippingModeBuilder {

    val enabled: DSLCommand
        get() {
            mode = ClippingMode.CLIP; return DSLCommand
        }

    val disabled: DSLCommand
        get() {
            mode = ClippingMode.SKIP; return DSLCommand
        }

}

