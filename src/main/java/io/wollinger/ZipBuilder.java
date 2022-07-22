package io.wollinger;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ZipBuilder {
    private File zipFile;
    private File newLocation;
    private final ArrayList<ZipperUpdateListener> listeners = new ArrayList<>();
    private ZipMethod method;
    private StandardCopyOption copyOption;

    enum ZipMethod {ZIP, UNZIP, COPY}

    public ZipBuilder() {
        //Empty constructor
    }

    public ZipBuilder(File zipFile) {
        setZipFile(zipFile);
    }

    public ZipBuilder(String zipFile) {
        setZipFile(zipFile);
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void setZipFile(String zipFile) {
        setZipFile(new File(zipFile));
    }

    public void setNewLocation(File newLocation) {
        this.newLocation = newLocation;
    }

    public void setNewLocation(String newLocation) {
        setNewLocation(new File(newLocation));
    }

    public void addUpdateListener(ZipperUpdateListener listener) {
        listeners.add(listener);
    }

    public void setMethod(ZipMethod method) {
        this.method = method;
    }

    public void setCopyOption(StandardCopyOption option) {
        this.copyOption = option;
    }

    public void build() throws ZipException, IOException {
        if(zipFile == null)
            throw new ZipException("ZipFile is null!");
        if(method == null)
            throw new ZipException("Method is null!");
        if(method != ZipMethod.COPY && newLocation == null)
            throw new ZipException("NewLocation cant be null unless method is Copy!");

        switch (method) {
            case ZIP -> ZipUtils.zip(zipFile, newLocation, listeners);
            case UNZIP -> ZipUtils.unzip(zipFile, newLocation, copyOption, listeners);
            case COPY -> System.out.println("Implement...");
        }
    }
}
