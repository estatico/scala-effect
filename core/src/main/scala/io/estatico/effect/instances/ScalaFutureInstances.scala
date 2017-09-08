package io.estatico.effect
package instances

import java.util.concurrent.TimeoutException

import scala.concurrent.{ExecutionContext, Future}

object ScalaFutureInstances extends ScalaFutureInstances

trait ScalaFutureInstances {

  /** Default instance for Async[Future] which uses a cache for the default ExecutionContext. */
  implicit def asyncFuture(implicit ec: ExecutionContext): Async[Future]
    = if (ec == ExecutionContext.global) defaultAsyncFuture else newAsyncFuture(ec)

  /** Cached instance for the default ExecutionContext. */
  private val defaultAsyncFuture: Async[Future] = newAsyncFuture(ExecutionContext.global)

  /** Constructs a new Async[Future] instance from an ExecutionContext. */
  def newAsyncFuture(implicit ec: ExecutionContext): Async[Future] = new Async[Future] {

    override def async[A](a: => A): Future[A] = Future(a)

    override def asyncBoth[A, B](fa: Future[A], fb: Future[B]): Future[(A, B)]
      = fa.flatMap(a => fb.map(b => (a, b)))

    override def race[A, B](fa: Future[A], fb: Future[B]): Future[Either[A, B]]
      = Future.firstCompletedOf(List(fa.map(Left(_)), fb.map(Right(_))))

    override def background[A](fa: Future[A]): Future[Unit]
      = Future.firstCompletedOf(List(Future.successful(()), fa)).map(_ => ())

    override def timeoutMillis[A](fa: Future[A])(millis: Long): Future[A] = {
      Future.firstCompletedOf(List(
        fa,
        Future {
          Thread.sleep(millis)
          throw new TimeoutException(s"Future timed out after $millis milliseconds")
        }
      ))
    }
  }

  /** Default instance for Sync[Future] */
  implicit val syncFuture: Sync[Future] = new Sync[Future] {
    override def sync[A](a: A): Future[A] = Future.successful(a)
  }

  /** Default instance for FromFuture[Future] */
  implicit val fromFutureFuture: FromFuture[Future] = new FromFuture[Future] {
    override def fromFuture[A](fa: Future[A]): Future[A] = fa
  }

  /** Default instance for Recoverable[Future] which uses a cache for the default ExecutionContext. */
  implicit def recoverableFuture(implicit ec: ExecutionContext): Recoverable[Future]
    = if (ec == ExecutionContext.global) defaultRecoverableFuture else newRecoverableFuture(ec)

  /** Cached instance for the default ExecutionContext. */
  private val defaultRecoverableFuture: Recoverable[Future]
    = newRecoverableFuture(ExecutionContext.global)

  /** Constructs a new Recoverable[Future] instance from an ExecutionContext. */
  def newRecoverableFuture(implicit ec: ExecutionContext): Recoverable[Future] = new Recoverable[Future] {

    override def fromEither[A](either: Either[Throwable, A]): Future[A] = either.fold(Future.failed, Future.successful)

    override def fail[A](e: Throwable): Future[A] = Future.failed(e)

    override def attempt[A](fa: Future[A]): Future[Either[Throwable, A]]
      = fa.map(Right(_)).recover { case e => Left(e) }

    override def attemptFold[A, B](fa: Future[A])(f: Throwable => B, g: A => B): Future[B]
      = fa.map(g).recover { case e => f(e) }

    override def attemptFoldWith[A, B](fa: Future[A])(f: Throwable => Future[B], g: A => Future[B]): Future[B]
      = fa.flatMap(g).recoverWith { case e => f(e) }

    override def handle[A](fa: Future[A])(f: PartialFunction[Throwable, A]): Future[A]
      = fa.recover(f)

    override def handleWith[A](fa: Future[A])(f: PartialFunction[Throwable, Future[A]]): Future[A]
      = fa.recoverWith(f)

    override def transform[A, B](fa: Future[A])(f: Throwable => Throwable, g: A => B): Future[B]
      = fa.transform(g, f)

    override def failMap[A](fa: Future[A])(f: Throwable => Throwable): Future[A]
      = fa.recoverWith { case e => Future.failed(f(e)) }

    override def mergeEither[A](fa: Future[Either[Throwable, A]]): Future[A]
      = fa.flatMap(_.fold(Future.failed, Future.successful))
  }
}
