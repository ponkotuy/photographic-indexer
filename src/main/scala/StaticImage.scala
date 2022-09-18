
import com.ponkotuy.app.CORSSetting
import com.ponkotuy.config.MyConfig
import org.scalatra.*

import java.nio.file.Paths

class StaticImage(conf: MyConfig) extends ScalatraServlet with CORSSetting {
  get("/*") {
    val path = multiParams("splat").head
    contentType = "image/jpeg"
    conf.app.photosDir.resolve(path).toFile
  }
}
