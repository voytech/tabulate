package io.github.voytech.tabulate.core.model.text

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

interface TextWrap {
    fun getId(): String
}

enum class DefaultTextWrap : TextWrap {
    BREAK_WORDS,
    BREAK_LINES,
    NO_WRAP;

    override fun getId(): String = name
}

interface TextWrapBuilder {
    var textWrap: TextWrap
}

interface DefaultTextWrapWords : TextWrapBuilder {
    val breakWords: DSLCommand
        get() {
            textWrap = DefaultTextWrap.BREAK_WORDS; return DSLCommand
        }

    val breakLines: DSLCommand
        get() {
            textWrap = DefaultTextWrap.BREAK_LINES; return DSLCommand
        }

    val noWrap: DSLCommand
        get() {
            textWrap = DefaultTextWrap.NO_WRAP; return DSLCommand
        }
}
