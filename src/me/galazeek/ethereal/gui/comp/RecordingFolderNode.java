package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.recording.RecordingFolder;

import java.util.Objects;

public class RecordingFolderNode extends Node {

    private RecordingFolder folder;

    public RecordingFolderNode(Object userObject, RecordingFolder folder) {
        super(userObject);
        this.folder = folder;
    }

    public boolean hasFolder() {
        return Objects.isNull(folder);
    }

    public RecordingFolder getFolder() {
        return folder;
    }
}
