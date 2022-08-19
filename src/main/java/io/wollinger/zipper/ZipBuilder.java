package io.wollinger.zipper;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {
    private final ArrayList<ZipperUpdateListener> listeners = new ArrayList<>();
    private boolean includeLog = false;

    @Getter
    private final ArrayList<File> input = new ArrayList<>();
    @Getter
    private final ArrayList<File> output = new ArrayList<>();
    @Getter
    private ZipMethod method;
    @Getter
    private StandardCopyOption copyOption;

    public ZipBuilder addInput(File file) {
        input.add(file);
        return this;
    }

    public ZipBuilder addInput(String file) {
        return addInput(new File(file));
    }

    public ZipBuilder addOutput(File file) {
        output.add(file);
        return this;
    }

    public ZipBuilder addOutput(String file) {
        return addOutput(new File(file));
    }

    public ZipBuilder setIncludeLog(boolean bool) {
        includeLog = bool;
        return this;
    }

    public ZipBuilder addUpdateListener(ZipperUpdateListener listener) {
        listeners.add(listener);
        return this;
    }

    public ZipBuilder setMethod(ZipMethod method) {
        this.method = method;
        return this;
    }

    public ZipBuilder setCopyOption(StandardCopyOption option) {
        this.copyOption = option;
        return this;
    }

    public void build() throws ZipException, IOException {
        if(method == null)
            throw new ZipException("Method is not set.");

        if(copyOption == null)
            copyOption = StandardCopyOption.REPLACE_EXISTING;

        switch(method) {
            case ZIP -> _zip();
            case UNZIP -> _unzip();
        }
    }

    private void _zip() throws ZipException, IOException {
        if(input.isEmpty() || output.isEmpty())
            throw new ZipException("Error zipping: Input or output is not set.");

        StringBuilder log = new StringBuilder("Method: " + method + "\n");
        log.append("Input:\n");
        for(File inputFile : input)
            log.append("+ ").append(inputFile).append("\n");
        log.append("Output:\n");
        for(File outputFile : output)
            log.append("+ ").append(outputFile).append("\n");

        File initialLocation = output.remove(0);
        ArrayList<File> filesToZip = new ArrayList<>();
        for(File inputFile : input)
            filesToZip.addAll(Utils.getSubFiles(inputFile));

        log.append("Starting...\n");

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(initialLocation));
        int index = 1;
        for(File file : filesToZip) {
            String name = file.getPath();
            //If we are on Windows and the file path is absolute we remove the : and add _DRIVE
            //So for example D:\test\ becomes D_DRIVE\test\
            if(file.isAbsolute() && Utils.isWindows())
                name = name.replaceFirst(Pattern.quote(":"), "_DRIVE");

            //In my testing the "name" starts with a "/". This results in a zip file
            //where the root folder starts with / or _. This ensures this does not happen
            if(name.charAt(0) == File.separatorChar)
                name = name.replaceFirst(Pattern.quote(File.separator), "");

            byte[] fileContent = Files.readAllBytes(file.toPath());

            for(ZipperUpdateListener listener : listeners)
                if(listener != null)
                    listener.update(name, index, filesToZip.size(), fileContent.length);

            log.append(String.format("[%s/%s] (%s) %s\n", index, filesToZip.size(), fileContent.length, name));

            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);
            out.write(fileContent, 0, fileContent.length);
            out.closeEntry();
            index++;
        }

        if(includeLog) {
            ZipEntry entry = new ZipEntry("ZipperLog.txt");
            out.putNextEntry(entry);
            out.write(log.toString().getBytes(), 0, log.toString().getBytes().length);
            out.closeEntry();
        }

        out.close();
        for(File o : output) {
            Utils.ensureFolder(o.getParentFile());
            Files.copy(initialLocation.toPath(), o.toPath(), copyOption);
        }

        //Add initialLocation back in case we run .build() again. Although you should not do that.
        output.add(initialLocation);
    }

    private void _unzip() throws ZipException, IOException {
        if(input.isEmpty() || output.isEmpty())
            throw new ZipException("Error unzipping: Input or output is not set.");

        for(File o : output) {
            Utils.ensureFolder(o);
            if(o.isFile())
                throw new ZipException("Error unzipping: Output should be a folder!");
        }

        File initialOutput = output.remove(0);

        for(File inputFile : input) {
            ZipFile zipFile = new ZipFile(inputFile.getAbsolutePath());
            ArrayList<ZipEntry> files = Utils.getSubFiles(zipFile);
            int index = 1;
            for(ZipEntry entry : files) {
                File newLocation = new File(initialOutput, entry.getName());
                Utils.ensureFolder(new File(newLocation.getParent()));
                InputStream stream = zipFile.getInputStream(entry);
                for(ZipperUpdateListener listener : listeners)
                    if(listener != null)
                        listener.update(entry.getName(), index, files.size(), stream.available());
                Files.copy(stream, newLocation.toPath(), copyOption);
                index++;
            }
        }

        for(File o : output) {
            Utils.ensureFolder(o);
            Files.walk(initialOutput.toPath()).forEach(a -> {
                if(!a.toFile().isDirectory()) {
                    String base = a.toAbsolutePath().toString().replaceFirst(Pattern.quote(initialOutput.getAbsolutePath()), "");
                    Path newPath = new File(o, base).toPath();
                    Utils.ensureFolder(newPath.toFile().getParentFile());
                    try {
                        Files.copy(a, newPath);
                    } catch(IOException exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }

        //add initial output back in case build() is called again.
        output.add(initialOutput);
    }
}
