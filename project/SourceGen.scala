import java.nio.file.{Files, Paths}

import sbt._

import scala.collection.JavaConverters._

object SourceGen {

  /**
   * Generates sources for scalaz71 from scalaz72.
   * Files that already exist in scalaz71 will not be copied from scalaz72.
   */
  def scalaz71(sourceManagedDir: File, sourceDir: File): Seq[File] = {
    val typ = sourceManagedDir.getName
    val base = Paths.get(s"scalaz72/src/$typ/scala")
    Files.walk(base).iterator().asScala.flatMap { p =>
      val relativized = base.relativize(p)
      val source = p.toFile
      val target = sourceManagedDir.toPath.resolve(relativized).toFile
      // Only copy scala files
      // Don't copy files which already exist in scalaz71
      val shouldCopy =
        source.getName.endsWith(".scala") &&
        !sourceDir.toPath.resolve(relativized).toFile.exists()
      if (shouldCopy) {
        IO.copyFile(source, target)
        Some(target)
      } else {
        None
      }
    }.toSeq
  }
}
