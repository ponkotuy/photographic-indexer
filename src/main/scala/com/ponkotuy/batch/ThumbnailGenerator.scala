package com.ponkotuy.batch

import sun.awt.image.ToolkitImage

import java.awt.image.{AreaAveragingScaleFilter, BufferedImage, FilteredImageSource, ImageObserver}
import java.awt.{Image, Toolkit, image}
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Path}
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}

case class ThumbnailGenerator(width: Int, height: Int) {
  private[this] val filter = AreaAveragingScaleFilter(width, height)

  def gen(path: Path): Array[Byte] = {
    val reader = ImageIO.getImageReadersBySuffix("jpg").next()
    reader.setInput(ImageIO.createImageInputStream(Files.newInputStream(path)))
    val metadata = reader.getImageMetadata(0)
    val image = reader.read(0)
    val producer = new FilteredImageSource(image.getSource, filter)
    val thumbnail = Toolkit.getDefaultToolkit.createImage(producer)
    val baos = new ByteArrayOutputStream()
    val ios = ImageIO.createImageOutputStream(baos)
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    writer.setOutput(ios)
    val iwParam = writer.getDefaultWriteParam
    iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    iwParam.setCompressionQuality(0.70f)
    val ir = thumbnail.asInstanceOf[ToolkitImage].getImageRep
    ir.reconstruct(ImageObserver.ALLBITS)
    writer.write(null, new IIOImage(ir.getOpaqueRGBImage, null, metadata), iwParam)
    baos.toByteArray
  }
}
