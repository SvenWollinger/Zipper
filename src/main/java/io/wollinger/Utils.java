package io.wollinger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Utils {
    //Unzip utility
    //file -> The file to unzip
    //extractDir -> The folder to extract to
    //copyOption -> StandardCopyOption. Replacing for example
    //listener -> Allows you to stay up to date with the progress
    public static void unzip(File file, File extractDir, StandardCopyOption copyOption, ArrayList<ZipperUpdateListener> listeners) {
        if(!ensureFolder(extractDir))
            return;

        if(copyOption == null)
            copyOption = StandardCopyOption.REPLACE_EXISTING;

        try {
            ZipFile zipFile = new ZipFile(file.getAbsolutePath());
            ArrayList<ZipEntry> files = getSubFiles(zipFile);
            int index = 1;
            for(ZipEntry entry : files) {
                File newLocation = new File(extractDir, entry.getName());
                ensureFolder(new File(newLocation.getParent()));
                InputStream stream = zipFile.getInputStream(entry);
                for(ZipperUpdateListener listener : listeners)
                    if(listener != null)
                        listener.update(entry.getName(), index, files.size(), stream.readAllBytes().length);
                Files.copy(stream, newLocation.toPath(), copyOption);
                index++;
            }
        } catch(IOException exception) {
            System.err.println("Error extracting zip file!");
            exception.printStackTrace();
        }
    }

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

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
