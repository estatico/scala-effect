package io.estatico.effect

import org.scalatest.{FunSuite, Matchers}
import org.typelevel.discipline.scalatest.Discipline

abstract class TestBase
  extends FunSuite
  with Matchers
  with Discipline
  with TestImports
