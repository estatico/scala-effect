package io.estatico.effect
package instances

import test._
import io.estatico.effect.laws.tests.RecoverableTests

import scalaz.effect.IO

class ScalazIOInstancesTest extends TestBase with ScalazIOInstances {

  implicit def arbIO[A](implicit arbA: Arbitrary[A]): Arbitrary[IO[A]] = Arbitrary(
    arbA.arbitrary.map(IO(_))
  )

  implicit def eqIO[A : Eq]: Eq[IO[A]] = Eq.instance { (x, y) =>

    def attemptRun(io: IO[A]): Either[Throwable, A] = try {
      Right(io.unsafePerformIO())
    } catch {
      case e: Throwable => Left(e)
    }

    attemptRun(x) === attemptRun(y)
  }

  checkAll("Recoverable[IO]", RecoverableTests[IO].recoverable[Int])
}
