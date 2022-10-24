package io.wollinger.zipper

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files

object JsonLoader {
    fun parse(file: File) {
        val settings = ObjectMapper().readValue(file, Settings::class.java)
        val builder = ZipBuilder(settings)

        val doLog = settings.log
        log(doLog, file, "Starting...")
        log(doLog, file, "Method: ${settings.method}")
        log(doLog, file, "Format Output: ${settings.formatOutput}")

        builder.getInputFiles().forEach { log(doLog, file, "Input += $it") }
        builder.getOutputFiles().forEach { log(doLog, file, "Output += $it") }

        if(doLog) {
            builder.addUpdateListener(object: ZipperUpdateListener{
                override fun update(currentFile: String, fileIndex: Int, totalFiles: Int, fileSize: Int) {
                    log(true, file, "[$fileIndex/$totalFiles] ($fileSize) $currentFile")
                }
            })
        }

        if(settings.includeConfig) {
            //ZIP only feature.
            //While UNZIP works with json files, i cant imagine a scenario where i would want the config to be included in the unzip
            //Might add if a need arises
            if(settings.method == ZipMethod.ZIP) {
                val zipperConfig = File("ZipperConfig.json")
                Files.copy(file.toPath(), zipperConfig.toPath())
                builder.addInput(zipperConfig)
                zipperConfig.deleteOnExit()
            }
        }
        builder.build()
    }

    private fun log(doLog: Boolean, file: File, message: String) {
        if(doLog)
            println("[${file.name}] $message")
    }
}