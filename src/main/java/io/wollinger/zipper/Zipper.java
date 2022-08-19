package io.wollinger.zipper;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class Zipper {
    private static final String VERSION = "0.0.1";

    public static void main(String[] args) {
        if(args.length == 0) {
            print("Not enough arguments! Try -help!");
            return;
        }

        if(args[0].equals("-help")) {
            print("Available arguments:");
            print("-help                                                - Prints this");
            print("-v / -version                                        - Prints version");
            print("");
            print("-json <path>                                         - Load a json file");
            print("-i <path>                                            - Add input file");
            print("-o <path>                                            - Add output file");
            print("-m <ZIP/UNZIP>                                       - Set method");
            print("-co <REPLACE_EXISTING/COPY_ATTRIBUTES/ATOMIC_MOVE>   - Set StandardCopyOption");
            print("-log <true/false>                                    - If you want logging of whats happening");
            return;
        }

        if(args[0].equals("-v") || args[0].equals("-version")) {
            print(VERSION);
            return;
        }

        if(args[0].equals("-json")) {
            try {
                JsonLoader.parse(new File(args[1]));
            } catch (IOException | ZipException e) {
                e.printStackTrace();
            }
            return;
        }

        ZipBuilder builder = new ZipBuilder();
        String priorType = null;
        for(String arg : args) {
            if(priorType == null) {
                switch(arg) {
                    case "-i" -> priorType = "input";
                    case "-o" -> priorType = "output";
                    case "-m" -> priorType = "method";
                    case "-co" -> priorType = "sco";
                    case "-log" -> priorType = "log";
                }
            } else {
                switch(priorType) {
                    case "input" -> builder.addInput(arg);
                    case "output" -> builder.addOutput(arg);
                    case "method" -> builder.setMethod(ZipMethod.valueOf(arg));
                    case "sco" -> builder.setCopyOption(StandardCopyOption.valueOf(arg));
                    case "log" -> {
                        if(Boolean.parseBoolean(arg)) {
                            builder.addUpdateListener((currentFile, fileIndex, totalFiles, fileSize) -> print(String.format("[%s/%s] (%s) %s", fileIndex, totalFiles, fileSize, currentFile)));
                        }
                    }
                }
                priorType = null;
            }
        }
        try {
            builder.build();
        } catch (ZipException | IOException e) {
            print("Error executing Builder!");
            e.printStackTrace();
        }
    }

    private static void print(String msg) {
        System.out.println(msg);
    }
}
