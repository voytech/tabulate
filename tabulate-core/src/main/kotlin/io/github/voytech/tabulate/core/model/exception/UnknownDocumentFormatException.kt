package io.github.voytech.tabulate.core.model.exception

/**
 * Exception thrown when [TabulationFormat] is not known.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
class UnknownDocumentFormatException: RuntimeException("Unknown tabulation format!")