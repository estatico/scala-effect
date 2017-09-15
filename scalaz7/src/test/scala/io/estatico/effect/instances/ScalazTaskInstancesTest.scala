package io.estatico.effect
package instances

import test._
import io.estatico.effect.laws.tests.RecoverableTests

import scalaz.\/
import scalaz.concurrent.Task

class ScalazTaskInstancesTest extends TestBase with ScalazTaskInstances {

  implicit def arbTask[A](implicit arbA: Arbitrary[A]): Arbitrary[Task[A]] = Arbitrary(
    arbA.arbitrary.map(Task.now)
  )

  implicit def eq_\/[L, R](implicit eq: Eq[Either[L, R]]): Eq[L \/ R] = Eq.instance(
    (x, y) => eq.eqv(x.toEither, y.toEither)
  )

  implicit def eqTask[A](implicit eqA: Eq[Throwable \/ A]): Eq[Task[A]] = Eq.instance(
    (x, y) => eqA.eqv(x.unsafePerformSyncAttempt, y.unsafePerformSyncAttempt)
  )

  checkAll("Recoverable[Task]", RecoverableTests[Task].recoverable[Int])
}
