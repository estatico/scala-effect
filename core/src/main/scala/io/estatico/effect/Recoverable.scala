package io.estatico.effect

/** Type class for recoverable effects. */
trait Recoverable[F[_]] {

  /** Create an effect from a pure Either value. */
  def fromEither[A](either: Either[Throwable, A]): F[A]

  /** Create a failed effect from an exception. */
  def fail[A](e: Throwable): F[A]

  /** Get access to any exceptions for this effect via Either. */
  def attempt[A](fa: F[A]): F[Either[Throwable, A]]

  /** Create a new effect by folding over an exception or successful result. */
  def attemptFold[A, B](fa: F[A])(f: Throwable => B, g: A => B): F[B]

  def attemptFoldWith[A, B](fa: F[A])(f: Throwable => F[B], g: A => F[B]): F[B]

  /** Recover the matched exception, returning a pure result. */
  def handle[A](fa: F[A])(f: PartialFunction[Throwable, A]): F[A]

  /** Recover the matched exception, returning a new effect. */
  def handleWith[A](fa: F[A])(f: PartialFunction[Throwable, F[A]]): F[A]

  /** Transform an effect using functions for its exception or successful result. */
  def transform[A, B](fa: F[A])(f: Throwable => Throwable, g: A => B): F[B]

  /** Map over the exception of an effect, if one exists. */
  def failMap[A](fa: F[A])(f: Throwable => Throwable): F[A]

  /** Convert an effectful Either into an effect, merging its exception into the effect. */
  def mergeEither[A](fa: F[Either[Throwable, A]]): F[A]
}

object Recoverable {

  def apply[F[_]](implicit ev: Recoverable[F]): Recoverable[F] = ev

  def fromEither[F[_], A](either: Either[Throwable, A])(implicit r: Recoverable[F]): F[A] = r.fromEither(either)

  def fail[F[_], A](e: Throwable)(implicit r: Recoverable[F]): F[A] = r.fail(e)
}


final class RecoverableOps[F[_], A](val repr: F[A]) extends AnyVal {

  def attempt(implicit r: Recoverable[F]): F[Either[Throwable, A]] = r.attempt(repr)

  def attemptFold[B](f: Throwable => B, g: A => B)(implicit r: Recoverable[F]): F[B] = r.attemptFold(repr)(f, g)

  def attemptFoldWith[B](f: Throwable => F[B], g: A => F[B])(implicit r: Recoverable[F]): F[B]
    = r.attemptFoldWith(repr)(f, g)

  def handle(f: PartialFunction[Throwable, A])(implicit r: Recoverable[F]): F[A] = r.handle(repr)(f)

  def handleWith(f: PartialFunction[Throwable, F[A]])(implicit r: Recoverable[F]): F[A] = r.handleWith(repr)(f)

  def transform[B](f: Throwable => Throwable, g: A => B)(implicit r: Recoverable[F]): F[B] = r.transform(repr)(f, g)

  def failMap(f: Throwable => Throwable)(implicit r: Recoverable[F]): F[A] = r.failMap(repr)(f)
}

/** Specialized ops class to help type inference for the .mergeEither extension method. */
final class RecoverableEitherOps[F[_], A](val repr: F[Either[Throwable, A]]) extends AnyVal {
  def mergeEither(implicit r: Recoverable[F]): F[A] = r.mergeEither(repr)
}

trait ToRecoverableOps {
  implicit def toRecoverableOps[F[_] : Recoverable, A](x: F[A]): RecoverableOps[F, A] = new RecoverableOps[F, A](x)
  implicit def toRecoverableEitherOps[F[_] : Recoverable, A](x: F[Either[Throwable, A]]): RecoverableEitherOps[F, A] = new RecoverableEitherOps[F, A](x)
}
