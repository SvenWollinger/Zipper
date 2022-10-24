package io.wollinger.zipper

import java.io.File
import java.nio.file.StandardCopyOption

object CLI {
    fun run(args : Array<String>) {
        if(args.isEmpty()) {
            println("Not enough arguments! Try -help!")
            return
        }

        if(args[0] == "-help") {
            println("Available arguments:")
            println("-help                                                - Prints this")
            println("-v / -version                                        - Prints version")
            println("")
            println("-json <path>                                         - Load a json file")
            println("-i <path>                                            - Add input file")
            println("-o <path>                                            - Add output file")
            println("-m <ZIP/UNZIP>                                       - Set method")
            println("-co <REPLACE_EXISTING/COPY_ATTRIBUTES/ATOMIC_MOVE>   - Set StandardCopyOption")
            println("-log <true/false>                                    - If you want logging of whats happening")
            return
        }

        if(args[0] == "-v" || args[0] == "-version") {
            println(VERSION)
            return
        }

        if(args[0] == "-json") {
            JsonLoader.parse(File(args[1]))
            return
        }

        val builder = ZipBuilder()
        var priorType: String? = null
        var log = false
        for(arg in args) {
            if(priorType == null) {
                when(arg) {
                    "-i" -> priorType = "input"
                    "-o" -> priorType = "output"
                    "-m" -> priorType = "method"
                    "-co" -> priorType = "sco"
                    "-log" -> priorType = "log"
                }
            } else {
                when(priorType) {
                    "input" -> builder.addInput(arg)
                    "output" -> builder.addOutput(arg)
                    "method" -> builder.setMethod(ZipMethod.valueOf(arg))
                    "sco" -> builder.setCopyOption(StandardCopyOption.valueOf(arg))
                    "log" -> {
                        log = true
                        if(arg.toBoolean()) {
                            builder.addUpdateListener(object: ZipperUpdateListener {
                                override fun update(currentFile: String, fileIndex: Int, totalFiles: Int, fileSize: Int) {
                                    println(String.format("[%s/%s] (%s) %s", fileIndex, totalFiles, fileSize, currentFile))
                                }
                            })
                        }
                    }
                }
                priorType = null
            }
        }

        if(log) {
            println("Method: ${builder.getMethod()}")
            println("StandardCopyOption: ${builder.getCopyOption()}")
            for(input in builder.getInputFiles())
                println("Input += $input")
            for(output in builder.getOutputFiles())
                println("Output += $output")
        }

        builder.build()
    }
}