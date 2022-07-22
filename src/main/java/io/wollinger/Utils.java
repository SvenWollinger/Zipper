package io.wollinger;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    //Returns an array of files in a ZipFile. Folders are ignored
    public static ArrayList<ZipEntry> getSubFiles(ZipFile zipFile) {
        ArrayList<ZipEntry> files = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if(!entry.isDirectory())
                files.add(entry);
        }
        return files;
    }

    //Returns an array of files on disk. Folders are ignored
    public static ArrayList<File> getSubFiles(File folder) {
        ArrayList<File> files = new ArrayList<>();

        if(!folder.isDirectory()) {
            files.add(folder);
            return files;
        }

        File[] folderFiles = folder.listFiles();
        if(folderFiles == null)
            return files;

        for (File folderFile : folderFiles)
            files.addAll(getSubFiles(folderFile));

        return files;
    }

    //Utility method for making sure a given folder exists
    //Throws error if folder can't be created or if "file" is not a folder
    //Returns true if process was successful
    public static boolean ensureFolder(File file) {
        if(!file.exists() && !file.mkdirs()) {
            System.err.printf("Error creating folder(s) for %s!", file.getAbsolutePath());
            return false;
        }
        if(!file.isDirectory()) {
            System.err.printf("ensureFolder: Path %s should probably be a folder!", file.getAbsolutePath());
            return false;
        }
        return true;
    }

    //Returns true if os.name contains windows
    private static String osName;
    public static boolean isWindows() {
        if(osName == null)
            osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }
}
