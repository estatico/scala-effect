package io.estatico.effect

import io.estatico.effect.laws.imports.LawTypes

import scala.util.{Failure, Success, Try}

trait EqInstances extends {}
  with LawTypes
  with TestTypes
  with TestSyntax
  with cats.kernel.instances.AllInstances
{

  implicit val eqThrowable: Eq[Throwable] = Eq.fromUniversalEquals

  implicit def eqEither[L : Eq, R : Eq]: Eq[Either[L, R]] = Eq.instance {
    case (Left(x), Left(y)) => x === y
    case (Right(x), Right(y)) => x === y
    case _ => false
  }

  implicit def eqTry[A : Eq]: Eq[Try[A]] = Eq.instance {
    case (Success(x), Success(y)) => x === y
    case (Failure(x), Failure(y)) => x === y
    case _ => false
  }
}
