package io.wollinger.zipper

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

class Settings {
    var includeConfig: Boolean = false
    var includeLog: Boolean = false
    var log: Boolean = false
    var formatOutput: Boolean = false
    var method: ZipMethod? = null
    var input = ArrayList<String>()
    var output = ArrayList<String>()
}

object JsonLoader {
    fun parse(file: File) {
        val settings = ObjectMapper().readValue(file, Settings::class.java)
        val builder = ZipBuilder()

        val doLog = settings.log
        log(doLog, file, "Starting...")
        log(doLog, file, "Method: ${settings.method}")
        log(doLog, file, "Format Output: ${settings.formatOutput}")

        builder.setIncludeLog(settings.includeLog)
        settings.input.forEach {
            val inputString = Utils.formatEnv(it)
            log(doLog, file, "Input += $inputString")
            builder.addInput(inputString)
        }
        settings.output.forEach {
            var outputString = Utils.formatEnv(it)
            outputString = if(settings.formatOutput) format(outputString) else outputString
            log(doLog, file, "Output += $outputString")
            builder.addOutput(outputString)
        }
        settings.method?.let { builder.setMethod(it) }

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
            if(builder.method == ZipMethod.ZIP) {
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

    private fun fZ(i: Int): String {
        return if(i > 9) i.toString() else "0$i"
    }

    private fun format(string: String): String {
        var result = string
        val now = LocalDateTime.now()

        result = result.replace("%dd%", fZ(now.dayOfMonth))
        result = result.replace("%mm%", fZ(now.monthValue))
        result = result.replace("%yyyy%", now.year.toString())

        result = result.replace("%hh%", fZ(now.hour))
        result = result.replace("%mm%", fZ(now.minute))
        result = result.replace("%ss%", fZ(now.second))
        result = result.replace("%sss%", fZ(now.nano))

        return result
    }
}