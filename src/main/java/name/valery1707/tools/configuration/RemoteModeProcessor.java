package name.valery1707.tools.configuration;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class RemoteModeProcessor {
    private final Pattern[] include;
    private final Pattern[] exclude;

    public RemoteModeProcessor(String includes, String excludes) {
        include = parseRegexps(includes);
        exclude = parseRegexps(excludes);
    }

    private Pattern[] parseRegexps(String s) {
        if (isEmpty(s)) {
            return new Pattern[0];
        }
        String[] patterns = s.split(";");
        Pattern[] result = new Pattern[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            result[i] = Pattern.compile(patterns[i]);
        }
        return result;
    }

    public boolean canProcess(String s) {
        return matchAny(include, s) && !matchAny(exclude, s);
    }

    private boolean matchAny(Pattern[] patterns, String s) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(s).matches()) {
                return true;
            }
        }
        return false;
    }
}
