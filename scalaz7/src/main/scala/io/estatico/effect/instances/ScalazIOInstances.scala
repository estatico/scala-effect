package io.estatico.effect
package instances

import scalaz.effect._

object ScalazIOInstances extends ScalazTaskInstances

trait ScalazIOInstances {

  /** Default instance for Recoverable[IO] */
  implicit val recoverableIO: Recoverable[IO] = new Recoverable[IO] {

    override def mergeEither[A](fa: IO[Either[Throwable, A]]): IO[A] =
      fa.flatMap(_.fold(IO.throwIO(_), IO(_)))

    override def attemptFoldWith[A, B](fa: IO[A])(f: (Throwable) => IO[B], g: (A) => IO[B]): IO[B] =
      fa.catchLeft.flatMap(_.fold(f, g))

    override def failMap[A](fa: IO[A])(f: (Throwable) => Throwable): IO[A] =
      fa.except{err => IO.throwIO(f(err))}

    override def handle[A](fa: IO[A])(f: PartialFunction[Throwable, A]): IO[A] =
      fa.except(err => IO(f(err)))

    override def fromEither[A](either: Either[Throwable, A]): IO[A] =
      mergeEither(IO(either))

    override def attempt[A](fa: IO[A]): IO[Either[Throwable, A]] =
      fa.catchLeft.map(_.toEither)

    override def fail[A](e: Throwable): IO[A] =
      IO.throwIO(e)

    override def transform[A, B](fa: IO[A])(f: (Throwable) => Throwable, g: (A) => B): IO[B] =
      failMap(fa)(f).map(g)

    override def handleWith[A](fa: IO[A])(f: PartialFunction[Throwable, IO[A]]): IO[A] =
      fa.except(f)

    override def attemptFold[A, B](fa: IO[A])(f: (Throwable) => B, g: (A) => B): IO[B] =
      fa.catchLeft.map(_.fold(f, g))
  }

  /** Default instance for Sync[IO] */
  implicit val syncIO: Sync[IO] = new Sync[IO] {

    override def sync[A](a: A): IO[A] = IO(a)
  }
}
