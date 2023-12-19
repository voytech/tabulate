package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.loadBufferedImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class Image(private val imageData: BufferedImage, private val boundingBox: RenderableBoundingBox) {

    constructor(renderable: Renderable<*>) : this(renderable.image(), renderable.boundingBox)

    constructor(uri: String, bbox: RenderableBoundingBox) : this(uri.loadBufferedImage(), bbox)

    fun measure(): RenderingResult {
        if (!boundingBox.isDefined()) {
            boundingBox.width = boundingBox.width ?: Width(imageData.width.toFloat(), UnitsOfMeasure.PT)
            boundingBox.height = boundingBox.height ?: Height(imageData.height.toFloat(), UnitsOfMeasure.PT)
        }
        return Ok.asResult()
    }
}

fun Renderable<*>.image(): BufferedImage {
    require(this is HasImage)
    return imageUri.loadBufferedImage()
}

fun <R> R.measureImage(): RenderingResult where R : Renderable<*>, R : HasImage = Image(this).measure()

fun ByteArray.measureImage(boundingBox: RenderableBoundingBox) =
    Image(ImageIO.read(ByteArrayInputStream(this)), boundingBox).measure()

fun String.measureImage(boundingBox: RenderableBoundingBox) = Image(this, boundingBox).measure()