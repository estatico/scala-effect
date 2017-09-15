package io.estatico.effect
package instances

import test._

import scalaz.\/
import scalaz.concurrent.Task

trait ScalazTestInstances {

  implicit def eqTask[A](implicit eq: Eq[Throwable \/ A]): Eq[Task[A]] = Eq.instance(
    (x, y) => eq.eqv(x.unsafePerformSyncAttempt, y.unsafePerformSyncAttempt)
  )
}
