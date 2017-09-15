package io.estatico.effect.laws.imports

import org.scalacheck.Prop

trait LawInstances extends LawTypes {

  implicit def toIsEqArrow[A](a: A): IsEqArrow[A] = new IsEqArrow(a)

  implicit def lawsIsEqToProp[A : Eq](isEq: IsEq[A]): Prop = cats.laws.discipline.catsLawsIsEqToProp(isEq)
}

/** Re-export of cats' IsEqArrow */
final class IsEqArrow[A](val repr: A) extends AnyVal {
  def <->(rhs: A): cats.laws.IsEq[A] = cats.laws.IsEqArrow(repr) <-> rhs
}
