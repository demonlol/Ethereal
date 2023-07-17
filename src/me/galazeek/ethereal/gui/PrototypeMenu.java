package me.galazeek.ethereal.gui;

import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.classes.course.Course;
import me.galazeek.ethereal.gui.action.MultiNodeRunnable;
import me.galazeek.ethereal.gui.action.NodeRunnable;
import me.galazeek.ethereal.gui.builder.*;
import me.galazeek.ethereal.gui.comp.*;
import me.galazeek.ethereal.gui.comp.MenuItem;
import me.galazeek.ethereal.gui.prompts.CreateClassPrompt;
import me.galazeek.ethereal.gui.prompts.obj.ClassData;
import me.galazeek.ethereal.gui.systemtray.TrayManager;
import me.galazeek.ethereal.recording.RecordingFolder;
import me.galazeek.ethereal.recording.RecordingManager;
import me.galazeek.ethereal.utils.FormattingUtils;
import me.galazeek.ethereal.utils.IOUtils2;
import me.galazeek.ethereal.utils.SwingUtils2;
import me.galazeek.ethereal.version.Version;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PrototypeMenu extends JFrame {

    private Tree tree;
    private JPanel panel;

//    todo
//    private MultiNodeRunnable loadRecordings;

    public PrototypeMenu() {}

    public void load() {
        MultiNodeRunnable loadRecordings = (node, courseName) -> {
            Course course = ClassesManager.GetCourse(courseName);
            if(course == null) {
                System.out.println("[PM] - Course is null");
                return;
            }
            //Remove all children from recordings subfolder
            node.removeAllChildren();

            //Sort by lastModified/lastCreated newest on top oldest on bottom
            List<File> audioFiles = Arrays
                    .stream(RecordingManager.GetAudioFiles(courseName))
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .collect(Collectors.toList());

            //Add folders first (before RecordingNode-s)
            RecordingFolder[] folders = course.getFolders();
            Map<RecordingFolderNode, List<String>> folderMap = new HashMap<>(); //Store all files that belong to which folder
            if(course.hasFolders()) {
                for (RecordingFolder folder : folders) {
                    RecordingFolderNode rfn = new NodeBuilder(folder.getFolderName())
                            .withListener((point, node13, name, doubleClick, leftClick, isFolder) -> {
                                if (!leftClick) {
                                    new PopupBuilder()
                                            .withItem(new MenuItemBuilder()
                                                    .withText("Delete Folder")
                                                    .withClickListener(() -> {
                                                        ClassesManager.DeleteFolder(course, folder);
                                                        tree.updateRecordings(node, courseName);
                                                        //TODO CLASSES DELETE FOLDER
                                                    })
                                                    .build())
                                            .build().show(tree, point.x, point.y);
                                }
                            })
                            .buildFolderNode(folder);
                    List<String> affected = folder.getAffectedFiles();

                    //Delete missing files from folder config
                    for (String s : affected) {
                        File f = new File(course.getCourseRecordingsDir() + "\\" + s);
                        if (!f.exists()) {
                            System.out.println("------------ Delete file " + s + " from folder " + folder.getFolderName() + " -------------------");
                            ClassesManager.DeleteUnknownFileFromFolder(course, folder, s);
                        }
                    }

                    folderMap.put(rfn, affected);

                    node.add(rfn); //Add folder to local Recordings node
                }
            }
            //Add all folders up here
            //Map<RecordingFolder, FileNames[]>

            for (File audioFile : audioFiles) {
                String fileName = audioFile.getName();

                RecordingFolderNode folderNode = null;
                main: for (Map.Entry<RecordingFolderNode, List<String>> entry : folderMap.entrySet()) {
                    for (String s : entry.getValue()) {
                        if(fileName.equalsIgnoreCase(s)) {
                            folderNode = entry.getKey();
                            break main;
                        }
                    }
                }
                boolean isRooted = folderNode == null; //If node is not within a folder

                RecordingFolderNode finalFolderNode = folderNode;
                System.out.println(finalFolderNode);
//                System.out.println(finalFolderNode.getFolder());
                RecordingNode audioFileNode = new NodeBuilder(audioFile.getName())
                        .withListener((point, node1, name, doubleClick, leftClick, isFolder) -> {
                            System.out.println("[Recording] - File node " + fileName + " selected.");
                        })
                        .withListener((point, node12, name, doubleClick, leftClick, isFolder) -> {
                            if(!leftClick) {
                                //Recording node options popup

                                File f = ClassesManager.FindFileInCourse(courseName, name);
                                if(f == null) {
                                    System.out.println("[PM] - Could not find \"" + name + "\" in " + courseName + " course files");
                                    return;
                                }
                                //USING COURSE OBJ, FIND FILE IN QUESTION

                                new PopupBuilder()
                                        .withItem(new MenuBuilder()
                                                .withText("Info")
                                                .withMenuItem(new MenuItemBuilder()
                                                        .withText("Total:")
                                                        .withMenuItemRunnable(mi -> mi.setText("Total: " + IOUtils2.getByteString(f.length())))
                                                        .build())
                                                .withMenuItem(new MenuItemBuilder()
                                                        .withText("Length:")
                                                        .withMenuItemRunnable(mi -> {
                                                            mi.setText("Length: " + FormattingUtils.getStringMs(IOUtils2.getAudioLength(f)));
                                                        }).build())
                                                .build())
                                        .withSeparator()
                                        .withItem(new MenuItemBuilder()
                                                .withText("Rename")
                                                .withClickListener(() -> {
                                                    String renamed = (String) JOptionPane.showInputDialog(
                                                            this,
                                                            "Enter new file name",
                                                            "Renaming Recording File...",
                                                            JOptionPane.PLAIN_MESSAGE,
                                                            null,
                                                            null,
                                                            "");

                                                    String ext = f.getName().substring(f.getName().indexOf('.'));
                                                    if(renamed == null || renamed.isEmpty()) return;
                                                    //Truncate extension if added in input
                                                    if(renamed.indexOf('.') != -1) renamed = renamed.substring(0, renamed.indexOf('.'));
                                                    File newFile = new File(f.getParentFile().getAbsolutePath() + "\\" + renamed + ext);
                                                    if(f.renameTo(newFile)) {
                                                        System.out.println("Successfully renamed file " + f.getName() + " to " + newFile.getName());

                                                        if(finalFolderNode != null) {
                                                            ClassesManager.RenameRecordingFile(course, f.getName(), newFile.getName(), finalFolderNode.getFolder());
                                                        }

                                                        tree.updateRecordings(node, courseName);
                                                    } else {
                                                        if(newFile.exists()) {
                                                            JOptionPane.showMessageDialog(this, "That filename already exists.", "Error", JOptionPane.ERROR_MESSAGE, null);
                                                        } else {
                                                            JOptionPane.showMessageDialog(this, "Failed to rename file.", "Error", JOptionPane.ERROR_MESSAGE, null);
                                                        }
                                                    }
                                                })
                                                .build())
                                        .withItem(new MenuItemBuilder()
                                                .withText("Delete")
                                                .withClickListener(() -> {
                                                    int result = JOptionPane.showConfirmDialog(
                                                            this,
                                                            "Are you sure?",
                                                            "Delete " + f.getName() + "?",
                                                            JOptionPane.YES_NO_OPTION);
                                                    if(result == JOptionPane.YES_OPTION) {
                                                        f.delete();
                                                        tree.updateRecordings(node, courseName);
                                                    }

                                                })
                                                .build())
                                        .withSeparator()
                                        .withItem(new MenuItemBuilder()
                                                .withText("Move To")
                                                .withMenuRunnable(m -> {
                                                    m.add(new MenuItemBuilder()
                                                            .isDisabled(isRooted)
                                                            .withText("Root")
                                                            .withClickListener(() -> {
                                                                if(finalFolderNode == null) return;
                                                                //Just remove file from folder's config
                                                                ClassesManager.MoveRecordingFile(course, fileName, finalFolderNode.getFolder(), null);

                                                                tree.updateRecordings(node, courseName);
                                                            }).build());

                                                    if(course.hasFolders()) {
                                                        for (RecordingFolder folder : folders) {
                                                            boolean disabled = finalFolderNode != null && folder.getUuid().equals(finalFolderNode.getFolder().getUuid());
                                                            MenuItem mi2 = new MenuItemBuilder()
                                                                    .isDisabled(disabled)
                                                                    .withText(folder.getFolderName())
                                                                    .withClickListener(() -> {
                                                                        if (disabled) return;
                                                                        //Remove file from folder's config, and update the new folder's config
                                                                        ClassesManager.MoveRecordingFile(course, fileName, finalFolderNode != null ? finalFolderNode.getFolder() : null, folder);

                                                                        tree.updateRecordings(node, courseName);
                                                                    }).build();
                                                            m.add(mi2);
                                                        }
                                                    }
                                                })
                                                .buildMenu())
                                        .build()
                                        .show(tree, point.x, point.y);
                            }
                        })
                        .buildRecordingNode(audioFile.getAbsolutePath());
                if(folderNode != null) {
                    folderNode.add(audioFileNode);
                } else {
                    node.add(audioFileNode);
                }
            }
            System.out.println("[Recording] - Re/loaded audio recordings (" + audioFiles.size() + " files)");

            if(tree != null) {
                tree.updateUI();
                tree.repaint();
            }
        };

        NodeRunnable loadTree = (n -> {
            n.removeAllChildren();

            Course[] courses = ClassesManager.GetCourses();
            if(courses == null) return;

            for (Course course : courses) {
                if(course.hasFolders()) {
                    for (RecordingFolder folder : course.getFolders()) {
                        System.out.println(folder.getFolderName() + ", " + folder.getUuid() + ", " + folder.getAffectedFiles());
                    }
                }
                Node courseNode = new NodeBuilder(course.getValues().getString("course_name"))
                        .withNode(new NodeBuilder("Info").build())
                        .withNode(new NodeBuilder("Assignments").build())
                        .withNode(new NodeBuilder("Grades").build())
                        .withNode(new NodeBuilder("Notes").build())
                        .withNode(new NodeBuilder("Media").build())
                        .withNode(new NodeBuilder("Recordings")
                                .withRunnable(node -> {
                                    loadRecordings.run(node, course.getCourseName()); //TODO IMPLEMENT CLASS LOADING (AFTER CLASSMANAGER)
                                })
                                .withListener((point, node, na, doubleClick, leftClick, isFolder) -> {
                                    if(!leftClick) {
                                        String name = course.getCourseName();
                                        new PopupBuilder()
                                                .withItem(new MenuBuilder()
                                                        .withText("Info")
                                                        .withMenuItem(new MenuItemBuilder()
                                                                .withText("Total:")
                                                                .withMenuItemRunnable(m -> {
                                                                    File[] files = course.getCourseRecordingsDir().listFiles();
                                                                    long totalBytes = 0;
                                                                    if(files != null && files.length > 0)
                                                                        totalBytes = Arrays.stream(files).map(File::length).mapToLong(fbytes -> fbytes).sum();
                                                                    m.setText("Total: " + IOUtils2.getByteString(totalBytes));
                                                                }).build())
                                                        .build())
                                                .withSeparator()
                                                .withItem(
                                                        new MenuItemBuilder().withText((RecordingManager.IsWorking(name) ? "Stop" : "Start") + " Recording")
                                                                .withClickListener(() -> {

                                                                    //Realtime recording and byte size visualizer
                                                                    Timer timer = new Timer(200, (a) -> {
                                                                        if(RecordingManager.IsWorking(name)) {
                                                                            String runtimeStr, sizeStr;
                                                                            if((runtimeStr = RecordingManager.GetRuntime(name)) != null) {
                                                                                if((sizeStr = RecordingManager.GetTotalBytes(name)) != null) {
                                                                                    node.setUserObject("Recordings [" + runtimeStr + "] [" + sizeStr + "]");
                                                                                } else {
                                                                                    node.setUserObject("Recordings [" + runtimeStr + "]");
                                                                                }
                                                                                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                                                                            }
                                                                        } else {
                                                                            node.setUserObject("Recordings");
                                                                            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                                                                            ((Timer) a.getSource()).stop();
                                                                        }
                                                                    });
                                                                    timer.start();

                                                                    System.out.println("[Recording] - IsWorking: " + RecordingManager.IsWorking(name));

                                                                    RecordingManager.ToggleRecording(name, recordingData -> {
                                                                        System.out.println("[Recording:PrototypeMenu] - Worker data received: " + recordingData);

                                                                        loadRecordings.run((Node) node, name);
                                                                    });
                                                                }).build())
                                                .withSeparator()
                                                .withItem(
                                                        new MenuItemBuilder().withText("Add Folder")
                                                                .withClickListener(() -> {
                                                                    String folderName = JOptionPane.showInputDialog(tree, "Folder name:", "New Folder", JOptionPane.PLAIN_MESSAGE);

                                                                    if(course.hasFolders()) {
                                                                        for (RecordingFolder folder : course.getFolders()) {
                                                                            if (folder.getFolderName().equalsIgnoreCase(folderName)) {
                                                                                JOptionPane.showMessageDialog(tree, "You cannot have duplicate\nfolder names.", "Error", JOptionPane.ERROR_MESSAGE, null);
                                                                                return;
                                                                            }
                                                                        }
                                                                    }

                                                                    ClassesManager.CreateNewFolder(course, folderName);

                                                                    loadRecordings.run((Node) node, name);
                                                                })
                                                        .build())
                                                .withItem(new MenuItemBuilder()
                                                        .withText("Delete Temp")
                                                        .withClickListener(() -> {
                                                            int i = 0;
                                                            File[] files = course.getCourseRecordingsDir().listFiles();

                                                            if(files != null && files.length > 0)
                                                                for (File file : files)
                                                                    if(file.getName().startsWith("temp"))
                                                                        if(file.delete())
                                                                            i++;
                                                            System.out.println("Deleted " + i + " temp files");
                                                        })
                                                        .build())
                                                .withSeparator()
                                                .withItem(new MenuItemBuilder()
                                                        .withText("Open In Explorer")
                                                        .withClickListener(() -> {
                                                            try {
                                                                Runtime.getRuntime().exec("explorer.exe \"" + course.getCourseRecordingsDir().getAbsolutePath() + "\"");
                                                            } catch (IOException e) {
                                                                throw new RuntimeException(e);
                                                            }
                                                        })
                                                        .build())
                                                .build().show(tree, (int) point.getX(), (int) point.getY());
                                    }
                                })
                                .build())
                        .buildRecordingNode(null);



                n.add(courseNode);
            }
        });

        Node classes = new NodeBuilder("Classes")
                .withListener((point, node, name, doubleClick, leftClick, isFolder) -> {
                    if(!leftClick) {
                        new PopupBuilder()
                                .withItem(new MenuItemBuilder()
                                        .withText("Add Class")
                                        .withClickListener(() -> {
                                            //TODO

                                            CreateClassPrompt prompt = new CreateClassPrompt(this);
                                            ClassData classData = prompt.get();

                                            //Refresh
                                            loadTree.run((Node) node);
                                            tree.updateUI();
                                            tree.repaint();

                                            System.out.println(classData);
                                        })
                                        .build())
                                .withItem(new MenuItemBuilder()
                                        .withText("Refresh")
                                        .withClickListener(() -> {
                                            loadTree.run((Node) node);
                                            tree.updateUI();
                                            tree.repaint();
                                        })
                                        .build())
                                .build().show(tree, point.x, point.y);
                    }
                })
                .withRunnable(loadTree)
                .build();

        Node email = new NodeBuilder("Email")
                .build();

        Node root = new NodeBuilder("Root")
                .withNode(classes)
                .withNode(email)
                .build();

//        tree = new Tree(classes);
        tree = new Tree(root);
        tree.setUIUpdateRunnable(loadRecordings);
        tree.addMouseListener(new TreeClickHandler());

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(600, 375));
        panel.setBackground(new Color(29, 29, 29));

        JScrollPane sptree = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sptree.setPreferredSize(new Dimension(250, 375));

        add(sptree);

//        JScrollPane sppanel = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        sppanel.setPreferredSize(new Dimension(600, 375));
        add(panel);
    }

    public void display() {
        setJMenuBar(new MenuBarBuilder()
                .withMenu(new MenuBuilder()
                        .withText("File")
                        .build())
                .withMenu(new MenuBuilder()
                        .withText("Edit")
                        .build())
                .withMenu(new MenuBuilder()
                        .withText("View")
                        .build())
                .withMenu(new MenuBuilder()
                        .withText("Settings")
                        .withMenuItem(new MenuItemBuilder()
                                .withText("Record Settings")
                                .withClickListener(() -> {
                                    try {
                                        System.out.println("notepad.exe \"" + RecordingManager.config.getCurrentPath() + "\"");
                                        Runtime.getRuntime().exec("notepad.exe \"" + RecordingManager.RECORD_CONFIG_PATH + "\"");
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .build())
                        .build())
                .build());
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            private AudioPlayer player;

            private String lastClassPath = "";
            private int lastCount = 0;

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                //Free up memory
                if(player != null) {
                    player.destroy();
                    player = null;
                }

                TreePath path = e.getPath();
                int count = path.getPathCount(), index = count - 1;
                if (count >= 2 + 1 /*compensate*/) {
                    String selectedClass = path.getPathComponent(1 /*to compensate for additional root*/+ 1).toString();
                    Course selectedCourse = ClassesManager.GetCourse(selectedClass.toLowerCase());

                    //Load panel
                    panel.removeAll();

                    JLabel label = new JLabel(selectedClass);
                    label.setFont(label.getFont().deriveFont(32f));
                    label.setForeground(Color.WHITE);
                    panel.add(label);

                    if(count >= 4 + 1) {
                        //Handle 4th row nodes
                        String fourthNode = path.getPathComponent(index).toString();
                        String thirdNode = path.getPathComponent(index - 1).toString();

                        //Handle files in folder node
                        boolean isInFolder = false;
                        if (selectedCourse.hasFolders()) {
                            for (RecordingFolder folder : selectedCourse.getFolders()) {
                                if(thirdNode.equals(folder.getFolderName())) {
                                    isInFolder = true;
                                    break;
                                }
                            }
                        }

                        //Handle node audio file clicks (starts with incase Recording "[XX:XX:XX] [XXMB/KB/ETC]"
                        if(thirdNode.startsWith("Recordings") || isInFolder) {
                            boolean isFile = fourthNode.endsWith("wav");
                            if(isFile) {
                                File classDir = selectedCourse.getCourseRecordingsDir();
                                if(classDir != null && classDir.exists()) {
                                    File foundFile = null;
                                    File[] files = classDir.listFiles();
                                    if(files != null && files.length > 0) {
                                        for (File file : files) {
                                            if(file.getName().equals(fourthNode)) {
                                                foundFile = file;
                                                break;
                                            }
                                        }
                                        if(foundFile != null) {
                                            try {
                                                player = new AudioPlayer(foundFile);
                                                panel.add(player);
                                            } catch (UnsupportedAudioFileException ex) {
                                                JOptionPane.showMessageDialog(null, "Audio file is invalid/corrupt/unsupported", "Error", JOptionPane.ERROR_MESSAGE);
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(null, "Could not locate file", "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                }
                            }
                        }

                    } else if(count >= 3) {
                        //Handle 3rd row nodes
                        String thirdNode = path.getPathComponent(index).toString();

                        if(thirdNode.equals("Info")) {
                            Method[] methods = Course.CourseInfo.class.getMethods();
                            for (int i = 0; i < methods.length; i++) {
                                Method method = methods[i];
                                if(!method.getName().startsWith("get")) continue;
                                if(method.getName().startsWith("getClass")) continue;

                                System.out.println(method.getName());
                                try {
                                    if(method.getParameterCount() > 0) continue; //Ignore parameter get functions
                                    Object get = method.invoke(selectedCourse.getCourseInfo());
                                    panel.add(SwingUtils2.MaxSeparator());
                                    panel.add(new JLabel(method.getName().substring(3) + " : " + get));
                                } catch (IllegalAccessException ex) {
                                    throw new RuntimeException(ex);
                                } catch (InvocationTargetException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Map<String, ChronoUnit> timeMap = Collections.unmodifiableMap(new HashMap<>(){{
                                put("Hours Left", ChronoUnit.HOURS);
                                put("Days Left", ChronoUnit.DAYS);
                                put("Weeks Left", ChronoUnit.WEEKS);
                                put("Months Left", ChronoUnit.MONTHS);
                            }});
                            Course.CourseInfo courseInfo = selectedCourse.getCourseInfo();
                            timeMap.entrySet().stream().forEach((set) -> {
                                String key = set.getKey();
                                ChronoUnit value = set.getValue();
                                panel.add(SwingUtils2.MaxSeparator());
                                panel.add(new JLabel(key + " : " + courseInfo.getTimeLeft(value)));
                            });
                        }
                    }

                    panel.updateUI();
                }
            }
        });

        TrayManager.getInstance();

        setLayout(new FlowLayout());
        setSize(900, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Ethereal v" + Version.getVersionString());
        setVisible(true);
    }

    private class TreeClickHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY()); //0=root, 1=inside root, etc
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY()); //tostr()=[Classes, English, Notes]

            tree.setSelectionPath(selPath);

            if(selRow != -1 && selPath != null) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                    if (node instanceof Node) { //Made from builder
                        ((Node) node).click(new Point(e.getX(), e.getY()), node, String.valueOf(selPath.getPathComponent(selPath.getPathCount() - 1)), true, false, node.isLeaf());
                    }
                } else if(SwingUtilities.isLeftMouseButton(e)) {
                    //Single Click
                    if(e.getClickCount() == 1) {}

                    //Double Click
                    else if(e.getClickCount() == 2) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                        if (node instanceof Node) { //Made from builder
                            ((Node) node).click(new Point(e.getX(), e.getY()), node, String.valueOf(selPath.getPathComponent(selPath.getPathCount() - 1)), true, true, node.isLeaf());
                        }
                    }
                }
            }
        }
    }

}
