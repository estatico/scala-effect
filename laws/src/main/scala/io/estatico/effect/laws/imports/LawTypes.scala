package io.estatico.effect.laws.imports

trait LawTypes {

  type IsEq[A] = cats.laws.IsEq[A]
  val IsEq = cats.laws.IsEq

  type Eq[A] = cats.Eq[A]
  val Eq = cats.Eq
}
