package io.estatico.effect

/**
 * Type class for effects which support synchronous execution.
 *
 * For types F which are also an Applicative, this provides something similar to
 * Applicative#point, the difference being that Sync.now will create a new
 * synchronous effect instead of running the effect asynchronously, which is
 * generally what point does. From the standpoint of async operations, the Sync
 * type class can be an optimization to avoid spinning up new threads where unnecessary.
 */
trait Sync[F[_]] {
  /** Create a synchronous effect from a pure value. */
  def sync[A](a: A): F[A]
}

trait SyncFunctions {
  /** Smart constructor for Sync effects. */
  def sync[F[_]]: SyncEffectBuilder[F] = SyncEffectBuilder.instance
}

final class SyncEffectBuilder[F[_]] private {
  def apply[A](a: A)(implicit ev: Sync[F]): F[A] = ev.sync(a)
}

object SyncEffectBuilder {

  def instance[F[_]]: SyncEffectBuilder[F] = _instance.asInstanceOf[SyncEffectBuilder[F]]

  private val _instance = new SyncEffectBuilder[Nothing]
}
