package me.galazeek.ethereal.gui.comp;

import java.io.File;

public class RecordingNode extends Node {

    private static final String[] audioFormats = {"mp3", "m4a", "aac", "wav", "aiff"};

    private String filePath;

    public RecordingNode(Object userObject, String recordingFilePath) {
        super(userObject);
        this.filePath = recordingFilePath;
    }

    public String getRecordingFilePath() {
        return filePath;
    }

    public static boolean IsAudioFile(String audioFilePath) {
        boolean b = false;
        for (String audioFormat : audioFormats) {
            if(audioFilePath.toLowerCase().endsWith(audioFormat)) b = true;
        }
        return b;
    }

}
