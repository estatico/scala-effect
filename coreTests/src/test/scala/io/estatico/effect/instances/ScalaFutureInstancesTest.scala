package io.estatico.effect
package instances

import io.estatico.effect.laws.tests.RecoverableTests

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ScalaFutureInstancesTest extends TestBase {

  import ScalaFutureInstancesTest._
  import ScalaFutureInstances._

  implicit val ec = ExecutionContext.global

  implicit def eqFuture[A : Eq]: Eq[Future[A]] = Eq.instance(
    (x, y) => FutureResult(x) == FutureResult(y)
  )

  checkAll("Recoverable[Future]", RecoverableTests[Future].recoverable[Int])
}

object ScalaFutureInstancesTest {

  sealed trait FutureResult[+A]
  object FutureResult {

    def apply[A](fa: Future[A]): FutureResult[A] = try {
      Success(Await.result(fa, 1.second))
    } catch {
      // Yes, catch all Throwables.
      case e: Throwable => Failure(e)
    }

    final case class Failure(e: Throwable) extends FutureResult[Nothing]
    final case class Success[A](value: A) extends FutureResult[A]
  }
}
