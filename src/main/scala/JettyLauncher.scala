import org.eclipse.jetty.ee10.servlet.DefaultServlet
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.scalatra.servlet.ScalatraListener

// Required only production
object JettyLauncher {
  def main(args: Array[String]): Unit = {
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

    val server = new Server(port)
    val context = new WebAppContext()

    context.setContextPath("/")
    context.setBaseResourceAsString(".")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}
