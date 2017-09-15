package io.estatico.effect
package laws
package tests

import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import org.typelevel.discipline.Laws

trait RecoverableTests[F[_]] extends Laws {

  def laws: RecoverableLaws[F]

  def recoverable[A : Eq : Arbitrary](
    implicit
    arbFA: Arbitrary[F[A]],
    eqFA: Eq[F[A]]
  ): RuleSet = {
    new DefaultRuleSet(
      name = "recoverable",
      parent = None,
      "failShortCircuit" -> forAll(laws.failShortCircuit[A] _),
      "attemptFoldThrowIdentity" -> forAll(laws.attemptFoldThrowIdentity[A] _),
      "failAttemptFoldIdentity" -> forAll(laws.failAttemptFoldIdentity[A] _),
      "attemptFoldWithFailIdentity" -> forAll(laws.attemptFoldWithFailIdentity[A] _),
      "failAttemptFoldWithIdentity" -> forAll(laws.failAttemptFoldWithIdentity[A] _)
    )
  }
}

object RecoverableTests {
  def apply[F[_] : Recoverable]: RecoverableTests[F] = new RecoverableTests[F] {
    override def laws: RecoverableLaws[F] = RecoverableLaws[F]
  }
}
