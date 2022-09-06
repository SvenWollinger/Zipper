package io.wollinger.zipper

import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

object JsonLoader {
    fun parse(file: File) {
        val jsonString = String(Files.readAllBytes(file.toPath()))
        val json = JSONObject(jsonString)
        val builder = ZipBuilder()

        //Setting: "method"
        //Takes: String to ZipMethod Enum
        val method = json.getString("method")
        builder.setMethod(ZipMethod.valueOf(method.uppercase()))

        //Setting: "log"
        //Takes: boolean
        val doLog = Utils.getJSONBoolean(json, "log", false)

        //Setting: "formatOutput"
        //Takes: boolean
        val formatOutput = Utils.getJSONBoolean(json, "formatOutput", false)

        //Setting: "includeConfig"
        //Takes: boolean
        val includeConfig = Utils.getJSONBoolean(json, "includeConfig", false)

        //Setting: "includeLog"
        //Takes: boolean
        val includeLog = Utils.getJSONBoolean(json, "includeLog", false)

        log(doLog, file, "Starting...")
        log(doLog, file, "Method: $method")
        log(doLog, file, "Format Output: $formatOutput")

        //Setting: "input"
        //Takes: String array
        val input = json.getJSONArray("input")
        for(i in 0 until input.length()) {
            val inputString = Utils.formatEnv(input.getString(i))
            log(doLog, file, "Input += $inputString")
            builder.addInput(inputString)
        }

        //Setting: "output"
        //Takes: String array
        val output = json.getJSONArray("output")
        for(i in 0 until output.length()) {
            var outputString = Utils.formatEnv(output.getString(i))
            outputString = if(formatOutput) format(outputString) else outputString
            log(doLog, file, "Output += $outputString")
            builder.addOutput(outputString)
        }

        if(doLog) {
            builder.addUpdateListener(object: ZipperUpdateListener{
                override fun update(currentFile: String, fileIndex: Int, totalFiles: Int, fileSize: Int) {
                    log(true, file, "[$fileIndex/$totalFiles] ($fileSize) $currentFile")
                }
            })
        }

        if(includeConfig) {
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
        builder.setIncludeLog(includeLog)
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