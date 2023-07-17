package me.galazeek.ethereal.recording;

import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.classes.course.Course;
import me.galazeek.ethereal.recording.listener.RecordingCompleteListener;
import me.galazeek.ethereal.utils.FormattingUtils;
import me.galazeek.ethereal.utils.IOUtils2;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RecordingManager {

    public static final String RECORD_CONFIG_PATH = ClassesManager.CLASSES_DIRECTORY + "\\record.yml";
    public static YamlConfiguration config = getConfig();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

    private static YamlConfiguration getConfig() {
        config = new YamlConfiguration();

        //Add defaults
        config.addDefaults(Collections.unmodifiableMap(new HashMap<>() {{
            put("audio_format.sampleRate", 44100);
            put("audio_format.sampleSizeInBits", 16);
            put("audio_format.channels", 2);
            put("audio_format.signed", true);
            put("audio_format.bigEndian", true);
            put("temp_file_bytes_cutoff", 2500000);
            put("audio_stream_buffer", 1024);
            put("debug", false);
            put("file_name", "{course_name} {MM-dd-yyyy} {rand}");
        }}));

        //Create classes.yml file
        File configFile = new File(RECORD_CONFIG_PATH);
        try {
            if (configFile.createNewFile()) {
                debug("[CM] - Created record.yml config file");
            }
        } catch (IOException e) {
            debug("[CM] - Failed to create record.yml configuration");
        }

        if (configFile.length() != 0) {
            //Load values from config
            try {
                config.load(RECORD_CONFIG_PATH);
                debug("[CM] - Loaded record.yml configuration file");

                config.save(RECORD_CONFIG_PATH);

                return config;
            } catch (IOException | InvalidConfigurationException e) {
                debug("[ClasssesManager] - Failed to load classes.yml configuration");
            }
        } else {
            //Save default values to config
            try {
                config.getDefaults().getValues(true).forEach(config::set);
                config.save(RECORD_CONFIG_PATH);
            } catch (IOException e) {
                debug("Failed to save");
            }
            return config;
        }
        return null;
    }

    private static Map<String, Boolean> runningWorkers = new HashMap<>();
    private static Map<String, Long[]> workersRuntime = new HashMap<>();

    private RecordingManager() {}

    public static void ToggleRecording(String courseName, RecordingCompleteListener listener) {
        Course course = ClassesManager.GetCourse(courseName);
        if(course == null) {
            debug("[RM] - Failed to find course");
            return;
        }
        boolean isRunningWorker = runningWorkers.containsKey(courseName) && runningWorkers.get(courseName);

        //Not rec. Start
        if(!isRunningWorker) {
            runningWorkers.put(courseName, true);

            SwingWorker worker = new SwingWorker<RecordingFinishedData, Void>() {

                List<File> tempFiles = new ArrayList<>();
                TargetDataLine line = null; //might need to move in worker class field and close in done()

                @Override
                protected RecordingFinishedData doInBackground() throws Exception {
                    long start = System.currentTimeMillis();

                    //TODO Name recording files to "{CLASS_NAME}_{CREATION DATE}.mp3" or something
                    String fileNameStr = config.getString("file_name")
                            .replace("{course_name}", course.getCourseName())
                            .replace("{MM-dd-yyyy}", sdf.format(new Date()))
                            .replace("{rand}", String.valueOf(System.currentTimeMillis()).substring(8));
                    String recordingFileName = fileNameStr + ".wav";
                    File recordingFile;

                    String recordingsDir = course.getCourseRecordingsDir().getAbsolutePath();
                    while ((recordingFile = new File(recordingsDir + "\\" + recordingFileName)).exists()) {
                        debug("[Recording] - File name " + recordingFileName + " exists. Renaming...");
                        recordingFileName = System.currentTimeMillis() + ".mp3";
                    }

                    debug("[Recording] - (check) Audio file \"" + recordingFileName + "\" doesn't exist. Creating now.");

                    if (!recordingFile.createNewFile()) {
                        debug("[Recording] - Failed to create audio recording file");
                        return null;
                    } else debug("[Recording] - Created new audio file");

                    AudioFormat format = new AudioFormat(
                            config.getFloat("audio_format.sampleRate"),
                            config.getInt("audio_format.sampleSizeInBits"),
                            config.getInt("audio_format.channels"),
                            config.getBoolean("audio_format.signed"),
                            config.getBoolean("audio_format.bigEndian")
                    );
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    // checks if system supports the data line
                    if (!AudioSystem.isLineSupported(info)) {
                        debug("Line not supported");
                        System.exit(0);
                    }
                    line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();   // start capturing

                    int tempFileSize = config.getInt("temp_file_bytes_cutoff");

                    File tempFile = new File(recordingFile.getParentFile().getAbsolutePath() + "\\temp_" + System.currentTimeMillis() + ".wav");
                    tempFile.createNewFile();

                    while (IsWorking(courseName)) {
                        debug("[Recording] - Recording audio...");
//                        Thread.sleep(150);

                        debug("Start capturing...");

                        try {
                            long totalBytes = 0;
                            long totalRuntime = System.currentTimeMillis();
                            long runtimeTimer = System.currentTimeMillis() + 1000;

                            FileOutputStream fos = new FileOutputStream(tempFile);
                            int buffer = config.getInt("audio_stream_buffer");
                            byte[] bytes = new byte[buffer];
                            int read;
                            while((read = line.read(bytes, 0, buffer)) != -1) {
                                fos.write(bytes, 0, bytes.length);

                                if(System.currentTimeMillis() - runtimeTimer < 0) {
                                    workersRuntime.put(courseName, new Long[]{(System.currentTimeMillis() - totalRuntime), totalBytes});
                                    runtimeTimer = System.currentTimeMillis() + 200;

                                    totalBytes = tempFile.length() + tempFiles.stream().map(File::length).mapToLong(fbytes -> fbytes).sum();
                                }
                                if(!IsWorking(courseName)) {
                                    tempFiles.add(tempFile);
                                    break;
                                }
                                if(fos.getChannel().size() >= tempFileSize) {
                                    Runtime runtime = Runtime.getRuntime();
                                    long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
//                                    debug("Used Memory before" + usedMemoryBefore);

                                    debug("Switching temp files...");
                                    tempFiles.add(tempFile);
//                                    for (Long aLong : tempFiles.stream().map(File::length).collect(Collectors.toList())) {
//                                        totalBytes += aLong;
//                                    }
//                                    debug("Total bytes so far: " + totalBytes);
                                    tempFile = new File(recordingFile.getParentFile().getAbsolutePath() + "\\temp_" + System.currentTimeMillis() + ".wav");
                                    tempFile.createNewFile();
                                    fos.flush();
                                    fos.close();
                                    fos = new FileOutputStream(tempFile);
                                    long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                                    debug("Memory increased:" + (usedMemoryAfter-usedMemoryBefore));
                                }
                            }
                            fos.flush();
                            fos.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    debug("[Recording] - Exiting worker thread...");
                    if(line != null) {
                        line.stop();
                        line.close();
                    }
                    long totalBytes = 0;
                    for (Long aLong : tempFiles.stream().map(File::length).collect(Collectors.toList())) {
                        totalBytes += aLong;
                    }
                    FileOutputStream fos = new FileOutputStream(recordingFile);
                    for (File tfile : tempFiles) {

                        debug("Writing " + tfile.length() + " bytes to " + recordingFile.getName());
                        final AudioInputStream in = new AudioInputStream(
                                new FileInputStream(tfile),
                                format,
                                totalBytes / format.getFrameSize()
                        );
                        AudioSystem.write(in, AudioFileFormat.Type.WAVE, fos);
                        fos.flush();
                        in.close();
                        tfile.delete();
                        debug("Deleted " + tfile.getName());
                    }
                    fos.close();
                    return new RecordingFinishedData(recordingFileName, start, System.currentTimeMillis());
                }

                @Override
                protected void done() {
                    debug("[Recording] - Finished recording for " + courseName);
                    try {
                        RecordingFinishedData rfd = get();
                        listener.complete(rfd);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    runningWorkers.remove(courseName);
                    workersRuntime.remove(courseName);

                    debug("Running Workers Size: " + runningWorkers.size());
                }
            };

            debug("[Recording] - Started recording for " + courseName);
            worker.execute();
        } else { //Rec. Stop
            debug("[Recording] - Stopping...");
            runningWorkers.remove(courseName);
            workersRuntime.remove(courseName);
        }
    }

    public static String GetRuntime(String course) {
        if(!workersRuntime.containsKey(course)) return null;
        long millis = workersRuntime.get(course)[0];
        return FormattingUtils.getStringMs(millis);
    }
    public static String GetTotalBytes(String course) {
        if(!workersRuntime.containsKey(course)) return null;
        long v = workersRuntime.get(course)[1];
        return IOUtils2.getByteString(v);
    }

    public static boolean IsWorking(String courseName) {
        return /*((workers.containsKey(courseName) && workers.get(courseName) != null) && */
                (runningWorkers.containsKey(courseName) && runningWorkers.get(courseName));
    }

    //Get folders TODO
//    public static

    public static File[] GetAudioFiles(String courseName) {
        Course course = ClassesManager.GetCourse(courseName);
        if(course == null) {
            debug("[RM] - Couldn't locate course " + courseName);
            return null;
        }
        //TODO class manager class file path
        return new File(course.getCourseRecordingsDir().getAbsolutePath()).listFiles();
    }

    private static void debug(String str) {
        if(config.getBoolean("debug")) {
            debug(str);
        }
    }

}
