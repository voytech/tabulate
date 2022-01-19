package io.github.voytech.tabulate.template.exception

/**
 * Exception thrown when there is no [OutputBinding] registered for given output class.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
class OutputBindingResolvingException: RuntimeException("Could not resolve OutputBinding!")