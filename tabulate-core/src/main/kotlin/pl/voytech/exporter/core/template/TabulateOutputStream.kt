package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.template.spi.Identifiable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class TabulateOutputStream(val id: String, private val wrapped: OutputStream): OutputStream(), Identifiable {

    constructor(file: File): this(file.extension, FileOutputStream(file))

    constructor(fileName: String): this(File(fileName))

    override fun write(b: Int) = wrapped.write(b)

    override fun close() = wrapped.close()

    override fun flush() = wrapped.flush()

    companion object {
        fun into(fileName: String) = TabulateOutputStream(fileName)
        fun into(file: File) = TabulateOutputStream(file)
        fun into(id: String, wrapped: OutputStream) = TabulateOutputStream(id, wrapped)
    }

    override fun getIdent(): String = id
}