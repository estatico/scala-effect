package io.estatico.effect
package instances

import scala.util.control.NonFatal
import scalaz.effect._

object ScalazIOInstances extends ScalazIOInstances

trait ScalazIOInstances {

  /** Default instance for Recoverable[IO] */
  implicit val recoverableIO: Recoverable[IO] = new ScalazDefaultRecoverable.FromMonad[IO] {

    override def fail[A](e: Throwable): IO[A] = IO.throwIO(e)

    override def attempt[A](fa: IO[A]): IO[Either[Throwable, A]]
      = nonFatal(fa.map(Right(_): Either[Throwable, A]))(e => IO(Left(e)))

    override def handle[A](fa: IO[A])(f: PartialFunction[Throwable, A]): IO[A]
      = nonFatal(fa)(e => IO(f.applyOrElse(e, throw e)))

    override def failMap[A](fa: IO[A])(f: Throwable => Throwable): IO[A]
      = nonFatal(fa)(e => IO.throwIO(f(e)))

    /** Similar to IO#except, but catches NonFatal instead of Throwable. */
    private def nonFatal[A](fa: IO[A])(f: Throwable => IO[A]): IO[A] = fa.except {
      case NonFatal(e) => f(e)
      case e => IO.throwIO(e)
    }
  }

  /** Default instance for Sync[IO] */
  implicit val syncIO: Sync[IO] = new Sync[IO] {
    override def sync[A](a: A): IO[A] = IO(a)
  }
}
