package io.estatico.effect

import scala.concurrent.Future

/** Type class for converting a Scala Future into an effect F. */
trait FromFuture[F[_]] {
  def fromFuture[A](fa: Future[A]): F[A]
}

object FromFuture {
  def apply[F[_]](implicit ev: FromFuture[F]): FromFuture[F] = ev
}

final class FromFutureOps[A](val repr: Future[A]) extends AnyVal {
  def futureTo[F[_]](implicit ev: FromFuture[F]): F[A] = ev.fromFuture(repr)
}

trait ToFromFutureOps {
  implicit def toFromFutureOps[A](x: Future[A]): FromFutureOps[A] = new FromFutureOps[A](x)
}
