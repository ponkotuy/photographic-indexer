import com.ponkotuy.app.*
import com.ponkotuy.batch.Initializer
import com.ponkotuy.config.MyConfig
import jakarta.servlet.ServletContext
import org.scalatra.*

import java.lang

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    Initializer.run()
    context.mount(new PhotographicIndexer, "/*")
  }
}
