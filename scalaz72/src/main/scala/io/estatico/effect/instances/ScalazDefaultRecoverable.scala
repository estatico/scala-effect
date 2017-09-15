package io.estatico.effect
package instances

object ScalazDefaultRecoverable {

  /** Default implementation for types which support Functor. */
  abstract class FromFunctor[F[_]](
    implicit F: scalaz.Functor[F]
  ) extends Recoverable[F] {

    override def attemptFold[A, B](fa: F[A])(f: Throwable => B, g: A => B): F[B]
      = F.map(attempt(fa))(_.fold(f, g))

    override def transform[A, B](fa: F[A])(f: Throwable => Throwable, g: A => B): F[B]
      = F.map(failMap(fa)(f))(g)
  }

  /** Default implementation for types which support Monad. */
  abstract class FromMonad[F[_]](
    implicit F: scalaz.Monad[F]
  ) extends FromFunctor[F] {

    override def fromEither[A](either: Either[Throwable, A]): F[A]
      = either.fold(fail, F.pure[A](_))

    override def attemptFoldWith[A, B](fa: F[A])(f: Throwable => F[B], g: A => F[B]): F[B]
      = F.bind(attempt(fa))(_.fold(f, g))

    override def handle[A](fa: F[A])(f: PartialFunction[Throwable, A]): F[A]
      = attemptFoldWith(fa)(e => if (f.isDefinedAt(e)) F.pure(f(e)) else fail(e), F.pure[A](_))

    override def handleWith[A](fa: F[A])(f: PartialFunction[Throwable, F[A]]): F[A]
      = F.join(handle(F.point(fa))(f))

    override def failMap[A](fa: F[A])(f: Throwable => Throwable): F[A]
      = attemptFoldWith(fa)(e => fail(f(e)), F.pure[A](_))

    override def mergeEither[A](fa: F[Either[Throwable, A]]): F[A]
      = F.bind(fa)(fromEither)
  }
}
