package net.thunderbird.cli.importer.translation

import java.io.File

object TranslationFileWriter {
    private val parser = TranslationKeyParser()

    fun writeFiles(outputRoot: File, files: List<ConsolidatedTranslationFile>): Int {
        outputRoot.mkdirs()
        var writtenCount = 0

        for (file in files) {
            val targetFile = File(outputRoot, file.filePath)
            val rendered = parser.renderStringsXml(file.keys)
            if (!targetFile.exists() || targetFile.readText() != rendered) {
                parser.writeStringsXml(targetFile, file.keys)
                writtenCount++
            }
        }

        return writtenCount
    }
}
