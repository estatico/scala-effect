package io.estatico.effect.test

import org.scalatest.{FunSuite, Matchers}
import org.typelevel.discipline.scalatest.Discipline

abstract class TestBase
  extends FunSuite
  with Matchers
  with Discipline
