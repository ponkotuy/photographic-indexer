import com.ponkotuy.app.*
import com.ponkotuy.batch.Initializer
import com.ponkotuy.config.MyConfig
import jakarta.servlet.ServletContext
import org.scalatra.*

import java.lang

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    context.mount(new PrivateImage(conf), "/app/images/*")
    context.mount(new PublicImage, "/app/public/images/*")
    context.mount(new PrivateStaticFile(conf.app), "/app/static/*")
    context.mount(new PublicStaticFile(conf.app), "/app/public/static")
    Initializer.run(conf)
  }
}
