package com.ponkotuy.batch

import com.ponkotuy.util.{ Extensions, RawPreviewExtractor }
import sun.awt.image.ToolkitImage

import java.awt.Toolkit
import java.awt.image.{ AreaAveragingScaleFilter, FilteredImageSource, ImageObserver }
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream }
import java.nio.file.{ Files, Path }
import javax.imageio.{ IIOImage, ImageIO, ImageWriteParam }

case class ThumbnailGenerator(width: Int, height: Int) {
  private val filter = AreaAveragingScaleFilter(width, height)

  def gen(path: Path): Array[Byte] = {
    val input =
      if (Extensions.isRawFile(path.toString)) rawThumbnailInput(path)
      else Files.newInputStream(path)
    try { gen(input) }
    finally { input.close() }
  }

  private def rawThumbnailInput(path: Path): InputStream = {
    val largest = RawPreviewExtractor.largest(path).getOrElse {
      throw new IllegalArgumentException(s"No embedded JPEG preview found in RAW file: ${ path }")
    }
    new ByteArrayInputStream(largest)
  }

  private def gen(input: InputStream): Array[Byte] = {
    val reader = ImageIO.getImageReadersBySuffix("jpg").next()
    val imageInput = ImageIO.createImageInputStream(input)
    try {
      reader.setInput(imageInput)
      val image = reader.read(0)
      val producer = new FilteredImageSource(image.getSource, filter)
      val thumbnail = Toolkit.getDefaultToolkit.createImage(producer)
      val baos = new ByteArrayOutputStream()
      val ios = ImageIO.createImageOutputStream(baos)
      val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
      try {
        writer.setOutput(ios)
        val iwParam = writer.getDefaultWriteParam
        iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
        iwParam.setCompressionQuality(0.70f)
        val ir = thumbnail.asInstanceOf[ToolkitImage].getImageRep
        ir.reconstruct(ImageObserver.ALLBITS)
        writer.write(null, new IIOImage(ir.getOpaqueRGBImage, null, null), iwParam)
        baos.toByteArray
      } finally {
        writer.dispose()
        ios.close()
      }
    } finally {
      reader.dispose()
      imageInput.close()
    }
  }
}
