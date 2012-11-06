package name.valery1707.tools.eset;

import org.apache.commons.io.FilenameUtils;
import org.ini4j.Profile;

import java.io.File;

import static name.valery1707.tools.Utils.canReadFile;

public class FileInfo implements Comparable<FileInfo> {
    public static final String OPT_FILE = "file";
    public static final String OPT_SIZE = "size";
    public static final String OPT_LANG = "language";
    public static final String OPT_VERSIONID = "versionid";

    private final String sectionName;
    private final Profile.Section section;

    public FileInfo(String sectionName, Profile.Section section) {
        this.sectionName = sectionName;
        this.section = section;
    }

    public String getSectionName() {
        return sectionName;
    }

    public Profile.Section getSection() {
        return section;
    }

    public String getUrl() {
        return section.get(OPT_FILE);
    }

    public boolean isLocalized() {
        return section.containsKey(OPT_LANG);
    }

    public String getFilename() {
        return FilenameUtils.getName(getUrl());
    }

    public String getLocalizedFilename() {
        return (isLocalized() ? section.get(OPT_LANG) + "/" : "") + getFilename();
    }

    public long sizeLocal(File dir) {
        File file1 = new File(dir, getFilename());
        File file2 = new File(dir, getLocalizedFilename());
        return canReadFile(file1) ? file1.length()
                : (canReadFile(file2) ? file2.length() : 0L);
    }

    public long sizeIni() {
        return section.get(OPT_SIZE, Long.class, -1L);
    }

    @Override
    public int compareTo(FileInfo o) {
        return this.sectionName.compareToIgnoreCase(o.getSectionName());
    }
}
