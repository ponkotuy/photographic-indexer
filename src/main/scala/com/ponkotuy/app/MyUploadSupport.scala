package com.ponkotuy.app

import jakarta.servlet.http.{HttpServletRequest, Part}
import org.scalatra.ScalatraServlet
import org.scalatra.servlet.FileUploadSupport.BodyParams
import org.scalatra.servlet.{FileItem, FileMultiParams, FileSingleParams, HasMultipartConfig, MultipartConfig, SizeConstraintExceededException}

import scala.jdk.CollectionConverters.*

trait MyUploadSupport extends HasMultipartConfig { self: ScalatraServlet =>
  import MyUploadSupport._

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(100 * 1024 * 1024)))

  protected def isSizeConstraintException(e: Exception): Boolean = e match {
    case _: IllegalStateException => true
    case _ => false
  }

  private def isMultipartRequest(req: HttpServletRequest): Boolean = {
    val isPostOrPut = Set("POST", "PUT", "PATCH").contains(req.getMethod)
    isPostOrPut && (req.contentType match {
      case Some(contentType) => contentType.startsWith("multipart/")
      case _ => false
    })
  }

  private def extractMultipartParams(req: HttpServletRequest): BodyParams = {
    req.get(BodyParamsKey).asInstanceOf[Option[BodyParams]] match {
      case Some(bodyParams) =>
        bodyParams

      case None => {
        val bodyParams = getParts(req).foldRight(BodyParams(FileMultiParams(), Map.empty)) {
          (part, params) =>
            val item = FileItem(part)

            if (!(item.isFormField)) {
              BodyParams(params.fileParams + ((
                  item.getFieldName, item +: params.fileParams.getOrElse(item.getFieldName, List[FileItem]()))), params.formParams)
            } else {
              BodyParams(params.fileParams, params.formParams)
            }
        }

        req.setAttribute(BodyParamsKey, bodyParams)
        bodyParams
      }
    }
  }

  private def getParts(req: HttpServletRequest): Iterable[Part] = {
    try {
      if (isMultipartRequest(req)) req.getParts.asScala else Seq.empty[Part]
    } catch {
      case e: Exception if isSizeConstraintException(e) => throw new SizeConstraintExceededException("Too large request or file", e)
    }
  }

  def fileMultiParams(implicit request: HttpServletRequest): FileMultiParams = {
    extractMultipartParams(request).fileParams
  }

  def fileMultiParams(key: String)(implicit request: HttpServletRequest): Seq[FileItem] = {
    fileMultiParams(request)(key)
  }

  /**
   * @return a Map, keyed on the names of multipart file upload parameters,
   *         of all multipart files submitted with the request
   */
  def fileParams(implicit request: HttpServletRequest): FileSingleParams = {
    new FileSingleParams(fileMultiParams(request))
  }

  def fileParams(key: String)(implicit request: HttpServletRequest): FileItem = {
    fileParams(request)(key)
  }
}

object MyUploadSupport {
  private val BodyParamsKey = "org.scalatra.fileupload.bodyParams"
}
