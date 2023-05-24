package io.github.voytech.tabulate.core.model.text

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

interface Font {
    val fontName: String
    fun getFontId(): String
}

enum class DefaultFonts(override val fontName: String): Font {
    TIMES_NEW_ROMAN("Times New Roman"),
    TIMES_ROMAN("Times Roman"),
    HELVETICA("Helvetica"),
    COURIER("Courier"),
    COURIER_NEW("Courier New"),
    ARIAL("Arial"),
    ARIAL_BLACK("Arial Black"),
    SYMBOL("Symbol"),
    CALIBRI("Calibri"),
    ZAPF_DINGBATS("Zapf Dingbats");
    override fun getFontId() = name

}

interface FontBuilder {
    var fontFamily: Font?
}

interface DefaultFontWords: FontBuilder {
    val timesNewRoman : DSLCommand
        get() {
            fontFamily = DefaultFonts.TIMES_NEW_ROMAN; return DSLCommand
        }

    val timesRoman : DSLCommand
        get() {
            fontFamily = DefaultFonts.TIMES_ROMAN; return DSLCommand
        }

    val arialBlack : DSLCommand
        get() {
            fontFamily = DefaultFonts.ARIAL_BLACK; return DSLCommand
        }

    val arial : DSLCommand
        get() {
            fontFamily = DefaultFonts.ARIAL; return DSLCommand
        }

    val helvetica : DSLCommand
        get() {
            fontFamily = DefaultFonts.HELVETICA; return DSLCommand
        }

    val courier : DSLCommand
        get() {
            fontFamily = DefaultFonts.COURIER; return DSLCommand
        }

    val courierNew : DSLCommand
        get() {
            fontFamily = DefaultFonts.COURIER_NEW; return DSLCommand
        }

    val calibri : DSLCommand
        get() {
            fontFamily = DefaultFonts.CALIBRI; return DSLCommand
        }
}