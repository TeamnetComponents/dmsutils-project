package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 6/24/2014.
 */
public enum VersioningType {

    NONE("none"), MAJOR("major"), MINOR("minor");

    private final static String VERSION_INITIAL = "0.0";
    private final static String VERSION_DELIMITER = ".";

    private final String value;

    public String getValue(){
        return value;
    }

    VersioningType(String value) {
        this.value = value;
    }

    public static VersioningType fromValue(String v) {
        if (v == null) {
            v = VersioningType.NONE.name();
        }
        for (VersioningType c : VersioningType.values()) {
            if (c.value.equals(v.toLowerCase())) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static String getNextVersion(String version, VersioningType versioningType) {
        String nextVersion = null;
        if (version == null) {
            version = VERSION_INITIAL;
        }
        String[] versionItems = version.split("\\" + VERSION_DELIMITER);
        if (versionItems.length != 2) {
            throw new RuntimeException("Incorrect version number");
        }
        int majorVersion = Integer.parseInt(versionItems[0]);
        int minorVersion = Integer.parseInt(versionItems[1]);
        if (VersioningType.MAJOR.equals(versioningType)) {
            majorVersion++;
            minorVersion = 0;
            nextVersion = "" + majorVersion + VERSION_DELIMITER + minorVersion;

        } else if (VersioningType.MINOR.equals(versioningType)) {
            //keep major version
            minorVersion++;
            nextVersion = "" + majorVersion + VERSION_DELIMITER + minorVersion;
        }
        return nextVersion;
    }


    public static void main(String[] args) {
        System.out.println(getNextVersion("1.2359", VersioningType.NONE));
        System.out.println(getNextVersion("1.2359", VersioningType.MINOR));
    }

}
