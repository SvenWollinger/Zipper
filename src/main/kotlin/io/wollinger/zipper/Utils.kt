package io.wollinger.zipper

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object Utils {

    //Returns an array of files in a ZipFile. Folders are ignored
    fun getSubFiles(zipFile: ZipFile): ArrayList<ZipEntry> {
        val files = ArrayList<ZipEntry>()
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if(!entry.isDirectory)
                files.add(entry)
        }
        return files
    }

    //Returns an array of files on disk. Folders are ignored
    fun getSubFiles(folder: File): ArrayList<File> {
        val files = ArrayList<File>()

        if(!folder.isDirectory) {
            files.add(folder)
            return files
        }

        val folderFiles = folder.listFiles() ?: return files
        for(folderFile in folderFiles)
            files.addAll(getSubFiles(folderFile))

        return files
    }

    //Utility method for making sure a given folder exists
    //Throws error if folder can't be created or if "file" is not a folder
    //Returns true if process was successful
    fun ensureFolder(file: File): Boolean {
        if(!file.exists() && !file.mkdirs()) {
            println("Error creating folder(s) for ${file.absolutePath}!")
            return false
        } else if(!file.isDirectory) {
            println("ensureFolder: Path ${file.absolutePath} should probably be a folder!")
            return false
        }
        return true
    }

    //Is true if os.name contains windows
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")

    fun formatEnv(string: String): String {
        var result = string
        result = formatEnvSingle(result, "appdata")
        result = formatEnvSingle(result, "localappdata")
        result = formatEnvSingle(result, "userprofile")
        result = formatEnvSingle(result, "programdata")
        return result
    }

    private fun formatEnvSingle(string: String, varr: String): String {
        val winVer = "%$varr%"
        var result = string
        if(result.lowercase().contains(winVer))
            result = result.replace(winVer, System.getenv(varr).replace("\\\\", "/"))
        return result
    }
}