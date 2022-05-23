package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider
import java.io.OutputStream


class PdfBoxOutputBindingsFactory : OutputBindingsProvider<PdfBoxRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<PdfBoxRenderingContext, *>> = listOf(
        PdfBoxOutputStreamOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> =
        DocumentFormat.format("pdf", "pdfbox")

}

class PdfBoxOutputStreamOutputBinding : OutputStreamOutputBinding<PdfBoxRenderingContext>() {
    override fun flush(output: OutputStream) {
        renderingContext.getCurrentContentStream().close()
        renderingContext.document.save(output)
        renderingContext.document.close()
    }
}
