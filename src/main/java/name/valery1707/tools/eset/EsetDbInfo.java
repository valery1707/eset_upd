package name.valery1707.tools.eset;

import name.valery1707.tools.CompressedInputStream;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static name.valery1707.tools.Utils.*;
import static name.valery1707.tools.eset.FileInfo.OPT_FILE;
import static name.valery1707.tools.eset.FileInfo.OPT_LANG;

public class EsetDbInfo extends Ini {
    public EsetDbInfo(File file) {
        super();
        //throws IOException, InvalidFileFormatException
        try {
            load(new CompressedInputStream(checkFile(file), file.length()));
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    public EsetDbInfo(List<FileInfo> files) {
        super();
        getConfig().setStrictOperator(true);
        for (FileInfo file : files) {
            Section section = file.getSection();
            put(file.getSectionName(), section);
            section.put(OPT_FILE, file.getFilename());//all files stored near update.ver
        }
    }

    private static FileInputStream checkFile(File file) {
        checkArgument(canReadFile(file), "Invalid file %s", file);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw propagate(e);
        }
    }

    public Integer getEngineVersion() {
        Integer value = get("ENGINE2", "versionid", Integer.class);
        checkArgument(value != null, "Incorrect update.ver format");
        return value;
    }

    public List<FileInfo> getFiles(List<String> langs) {
        List<FileInfo> list = new ArrayList<FileInfo>();
        for (Entry<String, Section> section : this.entrySet()) {
            if (isFileSection(section) && isIncludedLanguage(section, langs)) {
                list.add(new FileInfo(section.getKey(), section.getValue()));
            }
        }
        Collections.sort(list);
        return list;
    }

    private boolean isFileSection(Entry<String, Section> section) {
        return section.getValue().containsKey(OPT_FILE);
    }

    private boolean isIncludedLanguage(Entry<String, Section> sectionEntry, List<String> langs) {
        Section section = sectionEntry.getValue();
        return !section.containsKey(OPT_LANG) || langs.contains(section.get(OPT_LANG));
    }
}