package me.galazeek.ethereal.classes;

import me.galazeek.ethereal.classes.course.Course;
import me.galazeek.ethereal.exceptions.CourseNameExistsException;
import me.galazeek.ethereal.gui.prompts.obj.ClassData;
import me.galazeek.ethereal.io.FileManager;
import me.galazeek.ethereal.recording.RecordingFolder;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.configuration.MemorySection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

//The class file system and config manager, reader, writer

public class ClassesManager {

    private static ClassesManager instance = new ClassesManager();

    public static ClassesManager getInstance() { return instance; }

    public static final String CLASSES_DIRECTORY = FileManager.ROOT_DIRECTORY + "\\Classes";
    public static final String CONFIG_FILE_PATH = CLASSES_DIRECTORY + "\\classes.yml";

    public static final YamlConfiguration config = new YamlConfiguration();

    static {
        //todo fix this later. prettify
        File dir = new File(CLASSES_DIRECTORY);
        if(!dir.exists())
            if(dir.mkdirs()) System.out.println("[ClassesManager] - Created classes root directory");

        //Add defaults
        config.addDefaults(Collections.unmodifiableMap(new HashMap<>(){{
            put("classes", new String[]{});
        }}));

        //Create classes.yml file
        File configFile = new File(CONFIG_FILE_PATH);
        try {
            if(configFile.createNewFile()) {
                System.out.println("[CM] - Created classes.yml config file");
            }
        } catch (IOException e) {
            System.out.println("[CM] - Failed to create classes.yml configuration");
        }

        if(configFile.length() != 0) {
            //Load values from config
            try {
                config.load(CONFIG_FILE_PATH);
//                System.out.println("[CM] - Loaded classes.yml configuration file");

                System.out.println(config.getStringList("classes"));

                config.save(CONFIG_FILE_PATH);
            } catch (IOException | InvalidConfigurationException e) {
                System.out.println("[ClasssesManager] - Failed to load classes.yml configuration");
            }
        } else {
            //Save default values to config
            try {
                config.getDefaults().getValues(true).forEach(config::set);
                config.save(CONFIG_FILE_PATH);
            } catch (IOException e) {
                System.out.println("Failed to save");
            }
        }
    }

    public static void CreateCourse(ClassData cd) throws CourseNameExistsException {
        Course[] courses = GetCourses();
        if(courses != null)
            for (Course course : GetCourses())
                if(course.getCourseName().equalsIgnoreCase(cd.getName())) throw new CourseNameExistsException(course.getCourseName());

        String name = cd.getName();
        String lowercaseName = name.toLowerCase();

        File courseRoot = new File(CLASSES_DIRECTORY + "\\" + cd.getName());
        File courseRecordings = new File(courseRoot.getAbsolutePath() + "\\Recordings");

        courseRoot.mkdirs();
        courseRecordings.mkdirs();

        config.set("classes." + lowercaseName + ".course_name", cd.getName());
        config.set("classes." + lowercaseName + ".professor_name", cd.getTeacher());
        config.set("classes." + lowercaseName + ".start", cd.getStart().getTime());
        config.set("classes." + lowercaseName + ".end", cd.getEnd().getTime());
        config.set("classes." + lowercaseName + ".credit_hours", cd.getCredits());
//        config.set("classes." + lowercaseName + ".recording_folders", new MemorySection[]{});

        save();

//        System.out.println("Saved. Success!");
    }

    public static void DeleteUnknownFileFromFolder(Course course, RecordingFolder folder, String filename) {
        String pathfiles = "classes." + course.getCourseName().toLowerCase() + ".recording_folders." + folder.getUuid() + ".files";
        List<String> fileList = config.getStringList(pathfiles);
        fileList.remove(filename);
        config.set(pathfiles, fileList);
        save();
    }

    public static void DeleteFolder(Course course, RecordingFolder folder) {
        String pathuuid = "classes." + course.getCourseName().toLowerCase() + ".recording_folders." + folder.getUuid();
        config.set(pathuuid, null);
        save();
    }

    public static void CreateNewFolder(Course course, String folderName) {
        String uuid = UUID.randomUUID().toString();
        String pathfiles = "classes." + course.getCourseName().toLowerCase() + ".recording_folders." + uuid + ".files";
        String pathfolder = "classes." + course.getCourseName().toLowerCase() + ".recording_folders." + uuid + ".folder_name";

        config.set(pathfolder, folderName);
        config.set(pathfiles, new String[]{});

        save();
    }

    public static void RenameRecordingFile(Course course, String oldfilename, String newfilename, RecordingFolder folder) {
        String path = "classes." + course.getCourseName().toLowerCase() + ".recording_folders." + folder.getUuid() + ".files";

        List<String> ms = config.getStringList(path);
        ms.remove(oldfilename);
        ms.add(newfilename);

        config.set(path, ms);

        save();
    }

    public static void MoveRecordingFile(Course course, String filename, RecordingFolder oldfolder, RecordingFolder newfolder) {
        String courseName = course.getCourseName().toLowerCase();

        if(oldfolder != null) {
            //Move to local root Recordings folder
            String path = "classes." + courseName + ".recording_folders." + oldfolder.getUuid() + ".files";

            List<String> ms = config.getStringList(path);
            ms.remove(filename);
            config.set(path, ms);
        }
        if(newfolder != null) {
            String newpath = "classes." + courseName + ".recording_folders." + newfolder.getUuid() + ".files";

            List<String> ms = config.getStringList(newpath);
            ms.add(filename);
            config.set(newpath, ms);
        }
        save();
    }

    private ClassesManager() {}

    private static void save() {
        try {
            config.save(CONFIG_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //CASE SENSITIVE
    public static File FindFileInCourse(String courseName, String fileName) {
        Course course = ClassesManager.GetCourse(courseName);
        if(course == null) {
            System.out.println("[RM] - Couldn't locate course " + courseName);
            return null;
        }
        return findFile(fileName, course.getCourseRecordingsDir().listFiles());
    }

    private static File findFile(String filename, File[] files) {
        File f = null;
        for (File file : files) {
            if (file.isDirectory()) {
                findFile(filename, file.listFiles()); // Calls same method again.
            } else {
                if(file.getName().equals(filename)) {
                    f = file;
                    break;
                }
//                System.out.println("File: " + file.getAbsolutePath());
            }
        }
        return f;
    }

    public static Course GetCourse(String name) {
        for (Course course : GetCourses()) {
            if(course.getCourseName().equalsIgnoreCase(name)) return course;
        }
        return null;
    }

    public static Course[] GetCourses() {
        try {
            MemorySection ms = null;
            try {
                //Determines if classes is null
                System.out.println((config.getList("classes").size() == 0));
            } catch (NullPointerException npe) {
                try {
                    ms = (MemorySection) config.get("classes");
                } catch (ClassCastException cce) {
                    return null;
                }
            }

            Map<String, Object> values = null;
            if (ms != null) {
                values = ms.getValues(false);

                final List<Course> courses = new ArrayList<>();

                values.forEach((key, value) -> {
                    courses.add(new Course(key, ((MemorySection) value)));
                });

                Course[] arr = new Course[courses.size()];
//                System.out.println("[CM] - Loaded " + arr.length + " classes");
                return courses.toArray(arr);
            }
        } catch (ClassCastException cce) {
            System.out.println("[CM] There are no classes");
            cce.printStackTrace();
        }
        return null;
    }

}
