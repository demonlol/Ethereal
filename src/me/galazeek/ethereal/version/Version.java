package me.galazeek.ethereal.version;

public class Version {

    public static int MAJOR = 1;
    public static int MINOR = 0;
    public static int REVISION = 1;
//    public static int BUILD_NUMBER = 0;

    public static String getVersionString() {
        return MAJOR + "." + MINOR + "." + REVISION /* "." + BUILDERNUMBER*/;
    }

}
