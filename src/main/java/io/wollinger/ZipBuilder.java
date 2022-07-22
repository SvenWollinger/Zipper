package io.wollinger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {
    private final ArrayList<File> input = new ArrayList<>();
    private final ArrayList<File> output = new ArrayList<>();
    private final ArrayList<ZipperUpdateListener> listeners = new ArrayList<>();
    private ZipMethod method;
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
            case COPY -> _copy();
        }
    }

    private void _zip() throws ZipException, IOException {
        if(input.isEmpty() || output.isEmpty())
            throw new ZipException("Error zipping: Input or output is not set.");

        File initialLocation = output.remove(0);
        ArrayList<File> filesToZip = new ArrayList<>();
        for(File inputFile : input) {
            filesToZip.addAll(Utils.getSubFiles(inputFile));
        }

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(initialLocation));
        int index = 1;
        for(File file : filesToZip) {
            String name = file.getPath();
            //If we are on Windows and the file path is absolute we remove the : and add _DRIVE
            //So for example D:\test\ becomes D_DRIVE\test\
            if(file.isAbsolute() && Utils.isWindows())
                name = name.replaceFirst(Pattern.quote(":"), "_DRIVE");

            byte[] fileContent = Files.readAllBytes(file.toPath());
            //In my testing the "name" starts with a "/". This results in a zip file
            //where the root folder starts with / or _. This ensures this does not happen
            if(name.charAt(0) == File.separatorChar)
                name = name.replaceFirst(Pattern.quote(File.separator), "");

            for(ZipperUpdateListener listener : listeners)
                if(listener != null)
                    listener.update(name, index, filesToZip.size(), fileContent.length);

            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);
            out.write(fileContent, 0, fileContent.length);
            out.closeEntry();
            index++;
        }
        out.close();
        for(File o : output) {
            Utils.ensureFolder(o.getParentFile());
            Files.copy(initialLocation.toPath(), o.toPath(), copyOption);
        }

        //Add initialLocation back in case we run .build() again. Although you should not do that.
        output.add(initialLocation);
    }

    private void _unzip() {

    }

    private void _copy() {

    }
}
