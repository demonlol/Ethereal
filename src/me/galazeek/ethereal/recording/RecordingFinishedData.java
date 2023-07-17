package me.galazeek.ethereal.recording;

import java.io.File;

public class RecordingFinishedData {

    private String path;
    private long bytes;
    private long startMs, endMs; //current time millis

    public RecordingFinishedData(String path, long startMs, long endMs) {
        this.path = path;
        File f = new File(path);
        this.bytes = f.length();
        this.startMs = startMs;
        this.endMs = endMs;
    }

    public long getDurationInMS() { return this.endMs - this.startMs; }
    public String getPath() { return path; }
    public long getBytes() { return bytes; }

    @Override
    public String toString() {
        return "{Path:\"" + getPath() + "\", Bytes:" + getBytes() + ", Duration(ms):" + getDurationInMS() + " }";
    }
}
