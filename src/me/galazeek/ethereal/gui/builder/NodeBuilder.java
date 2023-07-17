package me.galazeek.ethereal.gui.builder;

import me.galazeek.ethereal.gui.action.NodeRunnable;
import me.galazeek.ethereal.gui.comp.Node;
import me.galazeek.ethereal.gui.comp.RecordingFolderNode;
import me.galazeek.ethereal.gui.comp.RecordingNode;
import me.galazeek.ethereal.gui.listener.TreeClickListener;
import me.galazeek.ethereal.recording.RecordingFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeBuilder {

    private String txt = null;
    private List<Node> children = null;
    private List<TreeClickListener> listeners;
    private NodeRunnable runnable;

    public NodeBuilder(String txt) {
        this.txt = txt;
    }
    private NodeBuilder() {}

    public NodeBuilder withNode(Node node) {
        if(children == null) children = new ArrayList<>();
        children.add(node);
        return this;
    }

    public NodeBuilder withNodes(Node[] nodes) {
        if(children == null) children = new ArrayList<>();
        children.addAll(Arrays.asList(nodes));
        return this;
    }

    public NodeBuilder withListener(TreeClickListener treeClickListener) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(treeClickListener);
        return this;
    }

    public NodeBuilder withRunnable(NodeRunnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public Node build() {
        if(txt != null) {
            Node node = new Node(txt);
            if (children != null) children.forEach(node::add);
            if (listeners != null) listeners.forEach(node::addClickListener);
            if(runnable != null) runnable.run(node);
            return node;
        }
        return null;
    }

    public RecordingNode buildRecordingNode(String audioFilePath) {
        RecordingNode node = new RecordingNode(txt, audioFilePath);
        if (children != null) children.forEach(node::add);
        if (listeners != null) listeners.forEach(node::addClickListener);
        if(runnable != null) runnable.run(node);
        return node;
    }

    public RecordingFolderNode buildFolderNode(RecordingFolder folder) {
        RecordingFolderNode node = new RecordingFolderNode(txt, folder);
        if (children != null) children.forEach(node::add);
        if (listeners != null) listeners.forEach(node::addClickListener);
        if(runnable != null) runnable.run(node);
        return node;
    }
}
