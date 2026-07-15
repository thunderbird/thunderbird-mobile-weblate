package net.thunderbird.cli.importer

import java.io.File

object ProjectRootResolver {
  fun resolve(startDir: File): File {
    var current = startDir.canonicalFile
    while (current.parentFile != null) {
      if (File(current, "settings.gradle.kts").exists() || File(current, ".git").exists()) {
        return current
      }
      current = current.parentFile
    }
    return startDir.canonicalFile
  }
}
