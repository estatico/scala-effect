package io.estatico.effect
package instances

import test._
import laws._
import laws.tests._
import org.scalatest.{FunSuite, Matchers}
import org.typelevel.discipline.scalatest.Discipline

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ScalaFutureInstancesTest extends FunSuite with Matchers with Discipline {

  import ScalaFutureInstancesTest._
  import ScalaFutureInstances._

  implicit val ec = ExecutionContext.global

  implicit def eqFuture[A : Eq]: Eq[Future[A]] = new Eq[Future[A]] {
    override def eqv(x: Future[A], y: Future[A]): Boolean = {
      FutureResult(x) == FutureResult(y)
    }
  }

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
