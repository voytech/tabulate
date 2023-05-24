package io.github.voytech.tabulate.core.model.clip

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

enum class DefaultClippingMode : ClippingMode {
    LETTERS,
    EDGE,
    NO_CLIP;

    override fun getId(): String = name
}

interface TextClippingModeBuilder {
    var mode: ClippingMode
}

interface TextClippingModeWords : TextClippingModeBuilder {

    val enabled: DSLCommand
        get() {
            mode = DefaultClippingMode.EDGE; return DSLCommand
        }

    val disabled: DSLCommand
        get() {
            mode = DefaultClippingMode.NO_CLIP; return DSLCommand
        }

}

