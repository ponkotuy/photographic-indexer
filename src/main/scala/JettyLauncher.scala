import org.eclipse.jetty.ee10.servlet.DefaultServlet
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.scalatra.servlet.ScalatraListener

// Required only production
object JettyLauncher {
  def main(args: Array[String]): Unit = {
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
    val viewDir = Option(System.getenv("ENV_VIEW_STATIC_DIR")).getOrElse("src/main/webapp")

    val server = new Server(port)
    val context = new WebAppContext()

    context.setContextPath("/")
    context.setBaseResourceAsString(viewDir)
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}
