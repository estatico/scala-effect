package io.estatico.effect

import io.estatico.effect.laws.imports.LawImports

trait TestImports extends {}
  with LawImports
  with TestInstances
  with TestSyntax
  with TestTypes
