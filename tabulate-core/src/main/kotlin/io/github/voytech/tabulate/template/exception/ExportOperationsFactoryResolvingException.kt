package io.github.voytech.tabulate.template.exception

/**
 * Exception thrown when there is no ExportOperationFactory registered for given format.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
class ExportOperationsFactoryResolvingException: RuntimeException("Could not resolve ExportOperationsFactory!")