package io.github.voytech.tabulate.core

/**
 * This class should provide instructions for rendering context implementation how to treat specific components,
 * in case when they cannot be treated in the same way as in other rendering contexts.
 * For example, we can easily handle rendering and exporting of Page, Table, Text or Image component for Pdf and Excel renderers,
 * but for CSV renderer we should apply specific strategies:
 * - Document may be treated as a root folder for example, or may be bypassed
 * - Page may be treated as a intermediate folder, or may be bypassed,
 * - Table should always export to single CSV file.
 * - Text, Image - should be skipped from export - there is no valid representation in simple CSV format.
 * For PDF for example - rendering context configuration may be useful for declaring page layout and size.
 **/

class RenderingContextConfiguration {

}