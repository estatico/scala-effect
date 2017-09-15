package io.estatico.effect
package laws

trait RecoverableLaws[F[_]] {

  import RecoverableLaws._
  import ToRecoverableOps._

  implicit def F: Recoverable[F]

  def failShortCircuit[A](fa: F[A]): IsEq[F[A]] =
    flatMap(F.fail(ex1))(_ => fa) <-> F.fail(ex1)

  def attemptFoldThrowIdentity[A](a: A): IsEq[F[A]] =
    pure(a).attemptFold[A](_ => throw ex1, _ => throw ex2) <-> F.fail(ex2)

  def failAttemptFoldIdentity[A](a: A): IsEq[F[A]] =
    F.fail[A](ex1).attemptFold[A](_ => throw ex2, _ => a) <-> F.fail(ex2)

  def attemptFoldWithFailIdentity[A](a: A): IsEq[F[A]] =
    pure(a).attemptFoldWith[A](_ => F.fail(ex1), _ => F.fail(ex2)) <-> F.fail(ex2)

  def failAttemptFoldWithIdentity[A](a: A): IsEq[F[A]] =
    F.fail[A](ex1).attemptFoldWith(_ => F.fail[A](ex2), _ => pure(a)) <-> F.fail(ex2)

  /** Simulate Applicative pure.  */
  protected def pure[A](a: A): F[A] = F.fromEither(Right(a))

  /** Simulate Monad flatMap. */
  protected def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] =
    fa.attemptFoldWith(F.fail, f)
}

object RecoverableLaws {

  def apply[F[_]](implicit ev: Recoverable[F]): RecoverableLaws[F] = new RecoverableLaws[F] {
    override implicit def F: Recoverable[F] = ev
  }

  /** Used to generate exceptions for laws. */
  private val ex1: Exception = SomeException(1)
  private val ex2: Exception = SomeException(2)

  private final case class SomeException(id: Int) extends Exception
}
