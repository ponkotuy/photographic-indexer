import com.ponkotuy.app.*
import com.ponkotuy.batch.Initializer
import com.ponkotuy.config.MyConfig
import jakarta.servlet.ServletContext
import org.scalatra.*

import java.lang

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    Initializer.run(conf)
    context.mount(new PhotographicIndexer(conf.app), "/app/*")
    context.mount(new StaticImage(conf), "/image/*")
  }
}
