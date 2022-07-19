package io.wollinger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static String getVersion() {
        return VERSION;
    }

    public static void zip(File toZip, File zipLocation, ZipperUpdateListener listener) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipLocation));
        ArrayList<File> filesToZip = getSubFiles(toZip);
        int index = 1;
        for(File file : filesToZip) {
            String name = file.getAbsolutePath().replaceAll(Pattern.quote(toZip.getParentFile().getAbsolutePath()), "");
            byte[] fileContent = Files.readAllBytes(file.toPath());
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

    public static void unzip(File file, File extractDir) {
        unzip(file, extractDir, StandardCopyOption.REPLACE_EXISTING, false);
    }

    public static void unzip(File file, File extractDir, StandardCopyOption copyOption, boolean printDebug) {
        if(!ensureFolder(extractDir))
            return;

        try {
            ZipFile zipFile = new ZipFile(file.getAbsolutePath());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File newLocation = new File(extractDir, entry.getName());
                if(!entry.isDirectory()) {
                    ensureFolder(new File(newLocation.getParent()));
                    if(printDebug)
                        System.out.printf("Extracting %s -> %s\n", entry.getName(), newLocation.getAbsolutePath());
                    Files.copy(zipFile.getInputStream(entry), newLocation.toPath(), copyOption);
                }
            }
        } catch(IOException exception) {
            System.err.println("Error extracting zip file!");
            exception.printStackTrace();
        }
    }

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
