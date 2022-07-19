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

public class Zipper {
    private static final String VERSION = "0.0.1";

    //Returns version
    public static String getVersion() {
        return VERSION;
    }

    //Zip utility
    //toZip -> The folder/file to zip
    //zipLocation -> Where to zip the file to. (for example: C:\test.zip)
    //listener -> Allows you to stay up to date with the progress
    public static void zip(File toZip, File zipLocation, ZipperUpdateListener listener) throws IOException {
        ensureFolder(zipLocation.getParentFile());

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipLocation));
        ArrayList<File> filesToZip = getSubFiles(toZip);
        int index = 1;
        for(File file : filesToZip) {
            String name = file.getAbsolutePath().replaceAll(Pattern.quote(toZip.getParentFile().getAbsolutePath()), "");
            byte[] fileContent = Files.readAllBytes(file.toPath());

            //In my testing the "name" starts with a "/". This results in a zip file
            //where the root folder starts with / or _. This ensures this does not happen
            if(name.charAt(0) == File.separatorChar)
                name = name.replaceFirst(Pattern.quote(File.separator), "");

            if(listener != null)
                listener.update(name, index, filesToZip.size(), fileContent.length);

            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);
            out.write(fileContent, 0, fileContent.length);
            out.closeEntry();
            index++;
        }
        out.close();
    }

    //Unzip utility
    //file -> The file to unzip
    //extractDir -> The folder to extract to
    //copyOption -> StandardCopyOption. Replacing for example
    //listener -> Allows you to stay up to date with the progress
    public static void unzip(File file, File extractDir, StandardCopyOption copyOption, ZipperUpdateListener listener) {
        if(!ensureFolder(extractDir))
            return;

        try {
            ZipFile zipFile = new ZipFile(file.getAbsolutePath());
            ArrayList<ZipEntry> files = getSubFiles(zipFile);
            int index = 1;
            for(ZipEntry entry : files) {
                File newLocation = new File(extractDir, entry.getName());
                ensureFolder(new File(newLocation.getParent()));
                InputStream stream = zipFile.getInputStream(entry);
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
    private static ArrayList<ZipEntry> getSubFiles(ZipFile zipFile) {
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
    private static ArrayList<File> getSubFiles(File folder) {
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
    private static boolean ensureFolder(File file) {
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
}
