package io.github.voytech.tabulate.template.context

/**
 * Marker interface for all table renderers with various public APIs.
 * Example implementation: `ApachePoiRenderingContext` - wrapper around apache poi streaming excel API.
 * Only contract it provides is that instance of this interface is receiver of all adaptation logic needed to perform
 * export with this third party library.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RenderingContext