package io.estatico.effect
package instances

import test._

import io.estatico.effect.laws.tests.RecoverableTests

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class ScalaFutureInstancesTest extends TestBase with ScalaFutureInstances {

  implicit val ec = ExecutionContext.global

  implicit def eqFuture[A](
    implicit eqA: Eq[Option[Try[A]]]
  ): Eq[Future[A]] = Eq.instance { (x, y) =>
    Await.ready(x, 1.second)
    Await.ready(y, 1.second)
    eqA.eqv(x.value, y.value)
  }

  checkAll("Recoverable[Future]", RecoverableTests[Future].recoverable[Int])
}
