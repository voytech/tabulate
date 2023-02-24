package io.github.voytech.tabulate.excel

import org.apache.poi.xssf.usermodel.*

class RichTextBox(
    drawing: XSSFDrawing,
    private val textBox: XSSFTextBox,
) : XSSFSimpleShape(drawing, textBox.ctShape) {

    var font: XSSFFont? = null

    fun append(string: String,font: XSSFFont? = null) {
        XSSFRichTextString().let { richText ->
            richText.append(string,font)
            textBox.setText(richText)
        }
    }

    fun applyFont(rendering: ApachePoiRenderingContext, block: (XSSFFont) -> Unit): XSSFFont =
        (font ?: rendering.workbook().createFont() as XSSFFont).apply {
            with(textBox.text) {
                font = apply(block)
                append(this,font)
            }
        }
}