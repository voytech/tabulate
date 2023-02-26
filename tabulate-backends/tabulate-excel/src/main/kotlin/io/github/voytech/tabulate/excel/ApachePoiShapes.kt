package io.github.voytech.tabulate.excel

import org.apache.poi.xssf.usermodel.*

class SimpleShapeWrapper(
    drawing: XSSFDrawing,
    private val shape: XSSFSimpleShape,
) : XSSFSimpleShape(drawing, shape.ctShape) {

    var font: XSSFFont? = null

    fun append(string: String,font: XSSFFont? = null) {
        XSSFRichTextString().let { richText ->
            richText.append(string,font)
            shape.setText(richText)
        }
    }

    fun applyFont(rendering: ApachePoiRenderingContext, block: (XSSFFont) -> Unit): XSSFFont =
        (font ?: rendering.workbook().createFont() as XSSFFont).apply {
            with(shape.text) {
                font = apply(block)
                append(this,font)
            }
        }
}