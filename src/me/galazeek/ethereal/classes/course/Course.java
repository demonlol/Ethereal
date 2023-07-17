package me.galazeek.ethereal.classes.course;

import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.classes.course.obj.Assignment;
import me.galazeek.ethereal.classes.course.obj.Media;
import me.galazeek.ethereal.classes.course.obj.Note;
import me.galazeek.ethereal.recording.RecordingFolder;
import org.bspfsystems.yamlconfiguration.configuration.MemorySection;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Course {

    //TODO (Assignments config ./english/assignments.yml)

    private String courseDir, courseRecordingsDir, notesDir, mediaDir, assignmentsDir;

    private String courseName;
    private MemorySection values; //{teacher_first=gfsdf, teacher_last=bszv, start=1688468719283}
    private CourseInfo courseInfo;

    private Assignment[] assignments;
    private Media[] medias;
    private Note[] notes;
    private RecordingFolder[] folders;

    public Course(String courseName, MemorySection values) {
        this.courseName = courseName;
        this.values = values;

        this.courseInfo = new CourseInfo(values);

        fileSetup();
        folderSetup();
    }

    private void folderSetup() {
        List<RecordingFolder> folderz = new ArrayList<>();

        if(!values.isSet("recording_folders")) return;

        Map<String, Object> folderUuids = ((MemorySection) values.get("recording_folders")).getValues(false);
        for (Map.Entry<String, Object> entry : folderUuids.entrySet()) {
            MemorySection folderValues = (MemorySection) entry.getValue();

            String uuid = entry.getKey();
            String folderName = folderValues.getString("folder_name");
            List<String> affectedFiles = folderValues.getStringList("files");

//            System.out.println(affectedFiles);

            RecordingFolder rf = new RecordingFolder(uuid, folderName, affectedFiles);
            folderz.add(rf);
        }
//        System.out.println("[Course] - Loaded " + folderz.size() + " folders");
        RecordingFolder[] rfolds = new RecordingFolder[folderz.size()];
        folderz.toArray(rfolds);
        this.folders = rfolds;
    }
    private void fileSetup() {
        this.courseDir = ClassesManager.CLASSES_DIRECTORY + "\\" + courseName;

        File courseFolder = new File(courseDir);

        if(courseFolder.mkdir()) {
            System.out.println("[Course:" + courseName + "] - Created course directory");
        }

        Map<String, String> directoryFields = Collections.unmodifiableMap(new HashMap<>(){{
            put("courseRecordingsDir", courseDir + "\\Recordings");
            put("notesDir", courseDir + "\\Notes");
            put("mediaDir", courseDir + "\\Media");
            put("assignmentsDir", courseDir + "\\Assignments");
        }});

        for (Map.Entry<String, String> entry : directoryFields.entrySet()) {
            String fieldName = entry.getKey();
            String directory = entry.getValue();

            try {
                //Set field names via reflection
                getClass().getDeclaredField(fieldName).set(this, directory);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            File file = new File(directory);
            if (file.mkdirs()) {
                System.out.println("Created " + fieldName + " directory: " + directory);
            }
        }
    }

    public boolean hasFolders() {
        return folders != null && folders.length > 0;
    }

    public File getCourseDir() { return new File(courseDir); }
    public File getCourseRecordingsDir() { return new File(courseRecordingsDir); }
    public MemorySection getValues() { return values; }
    public String getCourseName() { return courseName; }

    public RecordingFolder[] getFolders() { return folders; }
    public Assignment[] getAssignments() { return assignments; }
    public Media[] getMedias() { return medias; }
    public Note[] getNotes() { return notes; }

    public CourseInfo getCourseInfo() { return courseInfo; }

    public class CourseInfo {

        private String prettyCourseName, professorName;
        private double credits;
        private long start, end;

        public CourseInfo(MemorySection values) {
            this.prettyCourseName = values.getString("course_name");
            this.start = values.getLong("start");
            this.end = values.getLong("end");
            this.professorName = values.getString("professor_name");
            this.credits = values.getDouble("credits");
        }

        public String getPrettyCourseName() { return prettyCourseName; }
        public Date getStart() { return new Date(start); }
        public Date getEnd() { return new Date(end); }
        public String getProfessorName() { return professorName; }
        public double getCredits() { return credits; }
        public double getTimeLeft(ChronoUnit chronoUnit) {
            double remainder = 0;
            switch(chronoUnit) {
                case WEEKS:
                case MONTHS:
                    LocalDateTime startDate = getChronoCompatibleStartDate();
                    LocalDateTime endDate = getChronoCompatibleEndDate();

                    long res = chronoUnit.between(getChronoCompatibleStartDate(), getChronoCompatibleEndDate());

                    if(chronoUnit == ChronoUnit.WEEKS)
                        remainder = ChronoUnit.DAYS.between(startDate.plusWeeks(res), endDate) / 7.0;
                    else if(chronoUnit == ChronoUnit.MONTHS)
                        remainder = ChronoUnit.WEEKS.between(startDate.plusWeeks(res), endDate) / 4.0;

                    return (res + round(remainder, 2));
                default:
                    return chronoUnit.between(getStart().toInstant(), getEnd().toInstant());
            }
        }

        private double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }

        private LocalDateTime getChronoCompatibleStartDate() { return LocalDateTime.ofInstant(getStart().toInstant(), ZoneId.systemDefault()); }
        private LocalDateTime getChronoCompatibleEndDate() { return LocalDateTime.ofInstant(getEnd().toInstant(), ZoneId.systemDefault()); }

    }

}
