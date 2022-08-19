package io.wollinger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class JsonLoader {
    public static void parse(File file) throws IOException, ZipException {
        String jsonString = new String(Files.readAllBytes(file.toPath()));
        JSONObject json = new JSONObject(jsonString);
        ZipBuilder builder = new ZipBuilder();

        boolean doLog = false;
        try {
            doLog = json.getBoolean("log");
        } catch(JSONException ignored) { }

        log(doLog, file, "Starting...");

        boolean formatOutput = false;
        try {
            formatOutput = json.getBoolean("formatOutput");
        } catch(JSONException ignored) { }
        log(doLog, file, "Format Output: %s", formatOutput);

        JSONArray input = json.getJSONArray("input");
        for(int i = 0; i < input.length(); i++) {
            String inputString = input.getString(i);
            log(doLog, file, "Input += %s", inputString);
            builder.addInput(inputString);
        }

        JSONArray output = json.getJSONArray("output");
        for(int i = 0; i < output.length(); i++) {
            String outputString = output.getString(i);
            log(doLog, file, "Output += %s", outputString);
            builder.addOutput(formatOutput ? format(outputString) : outputString);
        }

        String method = json.getString("method");
        log(doLog, file, "Method: %s", method);
        builder.setMethod(ZipMethod.valueOf(method.toUpperCase()));
        if(doLog) {
            builder.addUpdateListener((currentFile, fileIndex, totalFiles, fileSize) -> {
                String msg = String.format("[%s/%s] (%s) %s", fileIndex, totalFiles, fileSize, currentFile);
                log(true, file, msg);
            });
        }
        builder.build();
    }

    private static void log(boolean doLog, File file, String message, Object... args) {
        if(doLog)
            System.out.printf("[%s] %s%n", file.getName(), String.format(message, args));
    }

    private static String fZ(int i) {
        if(i > 9) return Integer.toString(i);
        return "0" + i;
    }

    private static String format(String string) {
        LocalDateTime now = LocalDateTime.now();

        string = string.replaceAll("%dd%", fZ(now.getDayOfMonth()));
        string = string.replaceAll("%mm%", fZ(now.getMonthValue()));
        string = string.replaceAll("%yyyy%", Integer.toString(now.getYear()));

        string = string.replaceAll("%hh%", fZ(now.getHour()));
        string = string.replaceAll("%mm%", fZ(now.getMinute()));
        string = string.replaceAll("%ss%", fZ(now.getSecond()));
        string = string.replaceAll("%sss%", fZ(now.getNano()));

        return string;
    }
}
