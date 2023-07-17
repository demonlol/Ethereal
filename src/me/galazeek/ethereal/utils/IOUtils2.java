package me.galazeek.ethereal.utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class IOUtils2 {

    public static String getByteString(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    public static long getAudioLength(File file) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            long durationInMilliseconds = (int) Math.round((((frames + 0.0) / format.getFrameRate()) * 1000));
            return durationInMilliseconds;
        } catch (Exception ex) {
            return 0;
        }
    }

}
