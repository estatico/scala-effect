package io.estatico.effect
package instances

import test._

import scalaz.\/
import scalaz.concurrent.Task

trait ScalazTestInstances {

  /** Simulate methods from scalaz 7.2 */
  implicit class TaskTestOps[A](val repr: Task[A]) {
    def unsafePerformSyncAttempt: Throwable \/ A = \/.fromTryCatchThrowable[A, Throwable](repr.run)
  }

  implicit def eqTask[A](implicit eq: Eq[Throwable \/ A]): Eq[Task[A]] = Eq.instance(
    (x, y) => eq.eqv(x.unsafePerformSyncAttempt, y.unsafePerformSyncAttempt)
  )
}
