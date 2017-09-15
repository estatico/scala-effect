package io.estatico.effect

trait TestTypes {

  type Arbitrary[A] = org.scalacheck.Arbitrary[A]
  val Arbitrary = org.scalacheck.Arbitrary

  type Gen[A] = org.scalacheck.Gen[A]
  val Gen = org.scalacheck.Gen
}
