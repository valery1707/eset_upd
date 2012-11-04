package name.valery1707.tools.eset;

import name.valery1707.tools.CompressedInputStream;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static name.valery1707.tools.Utils.*;

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
            section.put("file", file.getFilename());//all files stored near update.ver
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

    public List<FileInfo> getFiles() {
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        for (Entry<String, Section> section : this.entrySet()) {
            if (section.getValue().containsKey("file")) {
                //todo skip non Russian files
                list.add(new FileInfo(section.getKey(), section.getValue()));
            }
        }
        return list;
    }
}
