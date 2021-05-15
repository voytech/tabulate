package io.github.voytech.exporter.pdf.api

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream

class PdfTableApi {
    private val document = PDDocument()
    private var xPos: Int = 0
    private var yPos: Int = 0
    private val contentStream: PDPageContentStream? = null
    private val currentPage: PDPage? = null

    fun createPage() : PDPageContentStream {
        return PDPage().let {
            document.addPage(it)
            PDPageContentStream(document, it)
        }
    }

    fun draw() {
        createPage().use {

        }
    }

    fun renderCellBorders(rowIndex: Int, columnIndex: Int, columnWidth: Int, rowHeight: Int) {

    }

    fun renderCellValue(rowIndex: Int, columnIndex: Int, columnWidth: Int, rowHeight: Int) {

    }

}
