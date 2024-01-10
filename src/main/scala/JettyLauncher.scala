import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher {
  def main(args: Array[String]): Unit = {
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
    val viewDir = Option(System.getenv("ENV_VIEW_STATIC_DIR")).getOrElse("src/main/webapp")

    val server = new Server(port)
    val context = new WebAppContext()

    context.setContextPath("/")
    context.setResourceBase(viewDir)
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}
