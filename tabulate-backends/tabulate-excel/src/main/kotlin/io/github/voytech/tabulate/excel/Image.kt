package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.loadImageAsByteArray
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class Image(private val imageData: ByteArray, private val boundingBox: RenderableBoundingBox) {

    constructor(renderable: Renderable<*>) : this(renderable.image(), renderable.boundingBox)

    constructor(uri: String, bbox: RenderableBoundingBox) : this(uri.loadImageAsByteArray(), bbox)

    fun measure(): RenderingResult {
        if (!boundingBox.isDefined()) {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(imageData))
            boundingBox.width = boundingBox.width ?: Width(bufferedImage.width.toFloat(),UnitsOfMeasure.PT)
            boundingBox.height = boundingBox.height ?: Height(bufferedImage.height.toFloat(),UnitsOfMeasure.PT)
        }
        return Ok.asResult()
    }
}

fun Renderable<*>.image(): ByteArray {
    require(this is HasImage)
    return imageUri.loadImageAsByteArray()
}

fun <R> R.measureImage(): RenderingResult where R: Renderable<*>, R: HasImage = Image(this).measure()

fun ByteArray.measureImage(boundingBox: RenderableBoundingBox) = Image(this,boundingBox).measure()

fun String.measureImage(boundingBox: RenderableBoundingBox) = Image(this,boundingBox).measure()