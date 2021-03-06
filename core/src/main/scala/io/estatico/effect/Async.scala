package io.estatico.effect

import scala.concurrent.duration.FiniteDuration

/** Type class for effects which support asynchronous execution. */
trait Async[F[_]] {

  /** Create an asynchronous effect from an unevaluated value. */
  def async[A](a: => A): F[A]

  /** Run two asynchronous effects in parallel. */
  def asyncBoth[A, B](fa: F[A], fb: F[B]): F[(A, B)]

  /** Run two asynchronous effects, returning the one which completes first. */
  def race[A, B](fa: F[A], fb: F[B]): F[Either[A, B]]

  /** Run an asynchronous effect in the background without waiting for it to complete. */
  def background[A](fa: F[A]): F[Unit]

  /** Timeout an asynchronous effect given a millisecond duration. */
  def timeoutMillis[A](fa: F[A])(millis: Long): F[A]

  /** Timeout an asynchronous effect given a finite duration. */
  final def timeout[A](fa: F[A])(d: FiniteDuration): F[A] = timeoutMillis(fa)(d.toMillis)
}

object Async {
  def apply[F[_]](implicit ev: Async[F]): Async[F] = ev
}

final class AsyncOps[F[_], A](val repr: F[A]) extends AnyVal {

  def background(implicit ev: Async[F]): F[Unit] = ev.background(repr)

  def timeoutMillis(millis: Long)(implicit ev: Async[F]): F[A] = ev.timeoutMillis(repr)(millis)

  def timeout(d: FiniteDuration)(implicit ev: Async[F]): F[A] = ev.timeout(repr)(d)
}

object ToAsyncOps extends ToAsyncOps

trait ToAsyncOps {
  implicit def toAsyncOps[F[_] : Async, A](x: F[A]): AsyncOps[F, A] = new AsyncOps(x)
}

object AsyncFunctions extends AsyncFunctions

trait AsyncFunctions {

  /** Smart constructor for Async effects. */
  def async[F[_]]: AsyncEffectBuilder[F] = AsyncEffectBuilder.instance[F]

  /** Shortcut for running Async[F].asyncBoth  */
  def asyncBoth[F[_]]: AsyncBothEffectBuilder[F] = AsyncBothEffectBuilder.instance[F]
}

final class AsyncEffectBuilder[F[_]] private {
  def apply[A](a: => A)(implicit ev: Async[F]): F[A] = ev.async(a)
}

object AsyncEffectBuilder {

  def instance[F[_]]: AsyncEffectBuilder[F] = _instance.asInstanceOf[AsyncEffectBuilder[F]]

  private val _instance = new AsyncEffectBuilder[Nothing]
}

final class AsyncBothEffectBuilder[F[_]] private {
  def apply[A, B](fa: F[A], fb: F[B])(implicit ev: Async[F]): F[(A, B)] = ev.asyncBoth(fa, fb)
}

object AsyncBothEffectBuilder {

  def instance[F[_]]: AsyncBothEffectBuilder[F] = _instance.asInstanceOf[AsyncBothEffectBuilder[F]]

  private val _instance = new AsyncBothEffectBuilder[Nothing]
}
