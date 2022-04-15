import zio._
import zio.console.putStrLn

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    putStrLn("Welcome to your first ZIO app!").exitCode
}