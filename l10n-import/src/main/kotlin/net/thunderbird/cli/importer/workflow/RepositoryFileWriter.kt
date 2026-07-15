package net.thunderbird.cli.importer.workflow

import org.slf4j.LoggerFactory
import java.io.File

object RepositoryFileWriter {
    private val logger = LoggerFactory.getLogger(RepositoryFileWriter::class.java)

    fun writeFiles(
        projectRoot: File,
        files: List<ResolvedRepositoryFile>,
    ): Int {
        var writtenCount = 0

        for (file in files) {
            val targetFile = File(projectRoot, file.relativePath)
            targetFile.parentFile?.mkdirs()
            if (!targetFile.exists() || targetFile.readText() != file.content) {
                targetFile.writeText(file.content)
                writtenCount++
            }
        }

        logger.info("Wrote {} files to {}", writtenCount, projectRoot.absolutePath)
        return writtenCount
    }
}
