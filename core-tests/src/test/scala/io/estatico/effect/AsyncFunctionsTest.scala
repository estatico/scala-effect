package io.estatico.effect

import org.scalatest.FlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class AsyncFunctionsTest extends FlatSpec {

  import instances.ScalaFutureInstances._
  import AsyncFunctions._

  implicit val ec = ExecutionContext.global

  "async" should "construct an Async effect" in {
    val fut = async[Future](1)
    assert(unsafeRunFuture(fut) == 1)
  }

  it should "infer its effect type if possible" in {
    assert(unsafeRunFuture(async("hello")) == "hello")
  }

  "asyncBoth" should "construct an Async effect from two Async effects" in {
    val fut = asyncBoth[Future](async(1), async(2))
    assert(unsafeRunFuture(fut) == (1, 2))
  }

  it should "infer its effect type if possible" in {

    // Infer from arguments
    val fut1 = async[Future](1)
    val fut2 = async[Future](2)
    val fut3 = asyncBoth(fut1, fut2)
    assert(unsafeRunFuture(fut3) == (1, 2))

    // Infer from caller
    assert(unsafeRunFuture(asyncBoth(async(1), async(2))) == (1, 2))
  }

  private def unsafeRunFuture[A](fa: Future[A]): A = Await.result(fa, 1.second)
}
