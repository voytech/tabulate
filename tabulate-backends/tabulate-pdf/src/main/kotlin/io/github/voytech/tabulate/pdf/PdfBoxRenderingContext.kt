package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.template.RenderingContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream


class PdfBoxRenderingContext(val document: PDDocument = PDDocument()): RenderingContext {

    private val pageContentStreams: MutableList<PDPageContentStream> = mutableListOf()

    fun addPage() = PDPage().apply {
        pageContentStreams.lastOrNull()?.let { it.close() }
        document.addPage(this)
        createContent().also { pageContentStreams.add(it) }
    }

    fun getCurrentContentStream(): PDPageContentStream = pageContentStreams.last()

    fun getPage(index: Int) = document.getPage(index)

    fun PDPage.createContent(): PDPageContentStream = PDPageContentStream(document, this)

}