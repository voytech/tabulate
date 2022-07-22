package io.github.voytech.tabulate.core.model.text

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