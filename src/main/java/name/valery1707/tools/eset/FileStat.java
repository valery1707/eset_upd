package name.valery1707.tools.eset;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.EnumMap;

import static name.valery1707.tools.Utils.byteCountForUser;

public class FileStat {
    public enum Type {DOWNLOADED, KEEPED, INACCESSIBLE, ERROR, REUSE_FROM_TMP}

    private int total = 0;
    private final EnumMap<Type, MutableInt> counts = new EnumMap<Type, MutableInt>(Type.class);
    private final EnumMap<Type, MutableLong> lengths = new EnumMap<Type, MutableLong>(Type.class);

    public FileStat() {
        for (Type type : Type.values()) {
            counts.put(type, new MutableInt(0));
            lengths.put(type, new MutableLong(0));
        }
    }

    public void touch(Type type) {
        total++;
        counts.get(type).increment();
    }

    public void touch(Type type, FileSizeInfo fileSizeInfo) {
        touch(type);
        lengths.get(type).add(fileSizeInfo.getRemote());
    }

    public Integer getCount(Type type) {
        return counts.get(type).getValue();
    }

    public Long getLength(Type type) {
        return lengths.get(type).getValue();
    }

    @Override
    public String toString() {
        return String.format("total: %d; inaccessible: %d; downloaded: %d [%s]; reused: %d [%s]; keeped: %d [%s]",
                total, counts.get(Type.INACCESSIBLE).getValue(),
                getCount(Type.DOWNLOADED), byteCountForUser(getLength(Type.DOWNLOADED)),
                getCount(Type.REUSE_FROM_TMP), byteCountForUser(getLength(Type.REUSE_FROM_TMP)),
                getCount(Type.KEEPED), byteCountForUser(getLength(Type.KEEPED))
        );
    }
}
