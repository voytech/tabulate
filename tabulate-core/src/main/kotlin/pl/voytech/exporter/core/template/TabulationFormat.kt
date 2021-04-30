package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.template.spi.Identifiable

class TabulationFormat<T,O>(
    private val formatId: String,
    val resultHandler: ResultHandler<T,O>
    ) : Identifiable {

    override fun getFormat(): String = formatId

}