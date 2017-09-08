package io.estatico.effect
package instances

import java.util.concurrent.ExecutorService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scalaz.{Nondeterminism, \/}
import scalaz.concurrent.{Strategy, Task}

object ScalazTaskInstances extends ScalazTaskInstances

trait ScalazTaskInstances {

  // Note that all of the exported names contain 'Scalaz' to avoid ambiguity with
  // other Task implementations.

  /** Default Async[Task] instance which uses a cache for the default ExecutorService. */
  implicit def asyncScalazTask(
    implicit pool: ExecutorService = Strategy.DefaultExecutorService
  ): Async[Task] = if (pool == Strategy.DefaultExecutorService) defaultAsyncScalazTask else newAsyncScalazTask(pool)

  /** Cached instance for the default Task pool. */
  private val defaultAsyncScalazTask: Async[Task] = newAsyncScalazTask(Strategy.DefaultExecutorService)

  /** Constructs an Async[Task] instance from an ExecutorService. */
  def newAsyncScalazTask(implicit pool: ExecutorService): Async[Task] = new Async[Task] {

    // Passing the pool here to explicitly demonstrate why we need an implicit pool to begin with.
    override def async[A](a: => A): Task[A] = Task(a)(pool)

    override def asyncBoth[A, B](fa: Task[A], fb: Task[B]): Task[(A, B)]
      = Nondeterminism[Task].both(fa, fb)

    override def race[A, B](fa: Task[A], fb: Task[B]): Task[Either[A, B]]
      = Nondeterminism[Task].choose(fa, fb).map(_.fold(t => Left(t._1), t => Right(t._2)))

    override def background[A](fa: Task[A]): Task[Unit]
      = Nondeterminism[Task].choose(Task.now(()), fa).map(_ => ())

    override def timeoutMillis[A](fa: Task[A])(millis: Long): Task[A]
      = fa.timed(millis)
  }

  /** Default FromFuture[Task] instance which uses a cache for the default ExecutionContext. */
  implicit def fromFutureScalazTask(implicit ec: ExecutionContext): FromFuture[Task]
    = if (ec == ExecutionContext.global) defaultFromFutureScalazTask else newFromFutureScalazTask(ec)

  /** Cached instance for the default ExecutionContext. */
  private val defaultFromFutureScalazTask: FromFuture[Task] = newFromFutureScalazTask(ExecutionContext.global)

  /** Constructs a FromFuture[Task] instance from an ExecutionContext. */
  def newFromFutureScalazTask(implicit ec: ExecutionContext): FromFuture[Task] = new FromFuture[Task] {
    override def fromFuture[A](fa: Future[A]): Task[A] = {
      Task.async { register =>
        fa.onComplete {
          case Failure(e) => register(\/.left(e))
          case Success(x) => register(\/.right(x))
        }
      }
    }
  }

  /** Default instance for Recoverable[Task] */
  implicit val recoverableScalazTask: Recoverable[Task] = new Recoverable[Task] {

    override def fromEither[A](either: Either[Throwable, A]): Task[A] = either.fold(Task.fail, Task.now)

    override def fail[A](e: Throwable): Task[A] = Task.fail(e)

    override def attempt[A](fa: Task[A]): Task[Either[Throwable, A]]
      = fa.attempt.map(_.toEither)

    override def attemptFold[A, B](fa: Task[A])(f: (Throwable) => B, g: A => B): Task[B]
      = fa.attempt.map(_.fold(f, g))

    override def attemptFoldWith[A, B](fa: Task[A])(f: Throwable => Task[B], g: A => Task[B]): Task[B]
      = fa.attempt.flatMap(_.fold(f, g))

    override def handle[A](fa: Task[A])(f: PartialFunction[Throwable, A]): Task[A]
      = fa.handle(f)

    override def handleWith[A](fa: Task[A])(f: PartialFunction[Throwable, Task[A]]): Task[A]
      = fa.handleWith(f)

    override def transform[A, B](fa: Task[A])(f: Throwable => Throwable, g: A => B): Task[B]
      = fa.attempt.flatMap(x => Task.fromDisjunction(x.bimap(f, g)))

    override def failMap[A](fa: Task[A])(f: Throwable => Throwable): Task[A]
      = fa.handleWith { case e => Task.fail(f(e)) }

    override def mergeEither[A](fa: Task[Either[Throwable, A]]): Task[A]
      = fa.flatMap(_.fold(Task.fail, Task.now))
  }

  /** Default instance for Sync[Task] */
  implicit val syncScalazTask: Sync[Task] = new Sync[Task] {
    override def sync[A](a: A): Task[A] = Task.now(a)
  }
}
