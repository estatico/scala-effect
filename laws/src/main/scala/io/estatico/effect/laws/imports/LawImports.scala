package io.estatico.effect.laws.imports

import org.scalacheck.Prop

trait LawImports {

  type IsEq[A] = cats.laws.IsEq[A]
  val IsEq = cats.laws.IsEq

  type Eq[A] = cats.Eq[A]
  val Eq = cats.Eq

  implicit def toIsEqArrow[A](a: A): IsEqArrow[A] = new IsEqArrow(a)

  implicit def lawsIsEqToProp[A : Eq](isEq: IsEq[A]): Prop = cats.laws.discipline.catsLawsIsEqToProp(isEq)
}

/** Re-export of cats' IsEqArrow */
final class IsEqArrow[A](val repr: A) extends AnyVal {
  def <->(rhs: A): cats.laws.IsEq[A] = cats.laws.IsEqArrow(repr) <-> rhs
}
