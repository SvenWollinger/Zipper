package io.wollinger;

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
            print("-i <path>                                            - Add input file");
            print("-o <path>                                            - Add output file");
            print("-m <ZIP/UNZIP>                                       - Set method");
            print("-co <REPLACE_EXISTING/COPY_ATTRIBUTES/ATOMIC_MOVE>   - Set StandardCopyOption");
            return;
        }

        if(args[0].equals("-v") || args[0].equals("-version")) {
            print(VERSION);
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
                }
            } else {
                switch(priorType) {
                    case "input" -> builder.addInput(arg);
                    case "output" -> builder.addOutput(arg);
                    case "method" -> builder.setMethod(ZipMethod.valueOf(arg));
                    case "sco" -> builder.setCopyOption(StandardCopyOption.valueOf(arg));
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
