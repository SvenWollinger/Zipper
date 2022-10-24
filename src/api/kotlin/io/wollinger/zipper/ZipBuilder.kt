package io.wollinger.zipper

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class Settings {
    var copyOption = StandardCopyOption.REPLACE_EXISTING
    var includeConfig = false
    var includeLog = false
    var log = false
    var formatOutput = false
    var method: ZipMethod? = null
    var input = ArrayList<String>()
    var output = ArrayList<String>()
}

enum class ZipMethod { ZIP, UNZIP }

class ZipBuilder(private val settings: Settings = Settings()) {
    private val listeners = ArrayList<ZipperUpdateListener>()

    fun addInput(file: String): ZipBuilder {
        settings.input.add(file)
        return this
    }

    fun addInput(file: File): ZipBuilder = addInput(file.absolutePath)

    fun addOutput(file: String): ZipBuilder {
        settings.output.add(file)
        return this
    }

    fun addOutput(file: File): ZipBuilder = addOutput(file.absolutePath)

    fun setIncludeLog(bool: Boolean): ZipBuilder {
        settings.includeLog = bool
        return this
    }

    fun addUpdateListener(listener: ZipperUpdateListener): ZipBuilder {
        listeners.add(listener)
        return this
    }

    fun setMethod(method: ZipMethod): ZipBuilder {
        settings.method = method
        return this
    }

    fun getMethod(): ZipMethod? = settings.method

    fun setCopyOption(option: StandardCopyOption): ZipBuilder {
        settings.copyOption = option
        return this
    }

    fun getCopyOption(): StandardCopyOption = settings.copyOption

    fun getInputFiles(): ArrayList<File> {
        val result = ArrayList<File>()
        settings.input.forEach { result.add(File(Utils.formatEnv(it))) }
        return result
    }

    fun getOutputFiles(): ArrayList<File> {
        val result = ArrayList<File>()
        settings.output.forEach {
            var path = Utils.formatEnv(it)
            path = if(settings.formatOutput) Utils.formatTime(path) else path
            result.add(File(path))
        }
        return result
    }

    fun build() {
        if(settings.method == null)
            throw ZipException("Method is not set.")

        when(settings.method) {
            ZipMethod.ZIP -> zip()
            ZipMethod.UNZIP -> unzip()
            else -> println("${settings.method} not valid/available!")
        }
    }

    private fun zip() {
        val input = getInputFiles()
        val output = getOutputFiles()

        if(input.isEmpty() || output.isEmpty())
            throw ZipException("Error zipping: Input or output is not set.")

        val log = StringBuilder("Method: ${settings.method}\n")
        log.append("Input:\n")
        for(inputFile in input)
            log.append("+ $inputFile\n")
        log.append("Output:\n")
        for(outputFile in output)
            log.append("+ $outputFile\n")

        val initialLocation = output.removeAt(0)
        val filesToZip = ArrayList<File>()
        for(inputFile in input)
            filesToZip.addAll(Utils.getSubFiles(inputFile))

        log.append("Starting...\n")

        val out = ZipOutputStream(FileOutputStream(initialLocation))
        var index = 1
        for(file in filesToZip) {
            var name = file.path
            //If we are on Windows and the file path is absolute we remove the : and add _DRIVE
            //So for example D:\test\ becomes D_DRIVE\test\
            if(file.isAbsolute && Utils.isWindows)
                name = name.replaceFirst(":", "_DRIVE")

            //In my testing the "name" starts with a "/". This results in a zip file
            //where the root folder starts with / or _. This ensures this does not happen
            if(name[0] == File.separatorChar)
                name = name.replaceFirst(File.separator, "")

            val fileContent = Files.readAllBytes(file.toPath())

            for(listener in listeners)
                listener.update(name, index, filesToZip.size, fileContent.size)

            log.append("[$index/${filesToZip.size}] (${fileContent.size}) $name\n")

            val entry = ZipEntry(name)
            out.putNextEntry(entry)
            out.write(fileContent, 0, fileContent.size)
            out.closeEntry()
            index++
        }

        if(settings.includeLog) {
            val logEntry = ZipEntry("ZipperLog.txt")
            out.putNextEntry(logEntry)
            val bytes = log.toString().toByteArray(Charset.defaultCharset())
            out.write(bytes, 0, bytes.size)
            out.closeEntry()
        }
        out.close()

        for(o in output) {
            Utils.ensureFolder(o.parentFile)
            Files.copy(initialLocation.toPath(), o.toPath(), settings.copyOption)
        }

        //Add initialLocation back in case we run .build() again. Although you should not do that.
        output.add(initialLocation)
    }

    private fun unzip() {
        val input = getInputFiles()
        val output = getOutputFiles()

        if(input.isEmpty() || output.isEmpty())
            throw ZipException("Error unzipping: Input or output is not set.")

        for(o in output) {
            if(o.isFile)
                throw ZipException("Error unzipping: Output should be a folder!")
            Utils.ensureFolder(o)
        }

        val initialOutput = output.removeAt(0)

        for(inputFile in settings.input) {
            val zipFile = ZipFile(File(inputFile).absolutePath)
            val files = Utils.getSubFiles(zipFile)
            var index = 1
            for(entry in files) {
                val newLocation = File(initialOutput, entry.name)
                Utils.ensureFolder(File(newLocation.parent))
                val stream = zipFile.getInputStream(entry)
                for(listener in listeners)
                    listener.update(entry.name, index, files.size, stream.available())
                Files.copy(stream, newLocation.toPath(), settings.copyOption)
                index++
            }
        }

        for(o in output) {
            Utils.ensureFolder(o)
            Files.walk(initialOutput.toPath()).forEach {
                if(!it.toFile().isDirectory) {
                    val base = it.toAbsolutePath().toString().replaceFirst(initialOutput.absolutePath, "")
                    val newPath = File(o, base).toPath()
                    Utils.ensureFolder(newPath.toFile().parentFile)
                    Files.copy(it, newPath)
                }
            }
        }

        //add initial output back in case build() is called again.
        output.add(initialOutput)
    }
}

interface ZipperUpdateListener {
    fun update(currentFile: String, fileIndex: Int, totalFiles: Int, fileSize: Int)
}