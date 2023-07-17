package me.galazeek.ethereal.sober;

import me.galazeek.ethereal.classes.ClassesManager;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SobrietyManager {

    private static final SobrietyManager sm = new SobrietyManager();

    public static SobrietyManager getInstance() { return sm; }

    private final String confPath = ClassesManager.CLASSES_DIRECTORY + "\\sobriety.yml";
    private YamlConfiguration conf;

    private SobrietyManager() {
        initIO();
    }

    private void initIO() {
        File confFile = new File(confPath);
        if(!confFile.exists()) {
            try {
                confFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        conf = YamlConfiguration.loadConfiguration(confFile);
    }

    public long getSobrietyDate() {
        long date = -1;
        if(!conf.isSet("date")) return -1;
        return date;
    }

    public void setSobrietyDate(long d) {
        conf.set("date", d);
        save();
    }

    public void resetSobrietyDate() {
        conf.set("date", System.currentTimeMillis());
        save();
    }

    private void save() {
        try {
            conf.save(confPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
