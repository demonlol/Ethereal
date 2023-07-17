package me.galazeek.ethereal.recording;

import java.util.List;

public class RecordingFolder {

    private String uuid;
    private String folderName;
    private List<String> affectedFiles;

    public RecordingFolder(String uuid, String folderName, List<String> affectedFiles) {
        this.uuid = uuid;
        this.folderName = folderName;
        this.affectedFiles = affectedFiles;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFolderName() {
        return folderName;
    }

    public List<String> getAffectedFiles() {
        return affectedFiles;
    }
}
