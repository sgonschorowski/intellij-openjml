package fh.luebeck.openjml.filter;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmlHyperlinkFilter implements Filter {
    //    static String regexUnix = ".?([[A-Z]:]?[/][^.]+.java):(\\d+)";
    static String regex = "([A-Z]?[:]?[\\\\/][^.]+.java):(\\d+)";
    private static Pattern pattern;

    private final Project myProject;

    public JmlHyperlinkFilter(Project project) {
        myProject = project;
//        if (SystemInfo.isMac || SystemInfo.isLinux) {
//            pattern = Pattern.compile(regexUnix);
//        } else {
        pattern = Pattern.compile(regex);
//        }
    }

    @Nullable
    @Override
    public Result applyFilter(String line, int entireLength) {
        List<ResultItem> resultItemList = new ArrayList<>();
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            int lineNumber = NumberUtils.createInteger(matcher.group(2)) - 1;
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (file == null) {
                continue;
            }
            resultItemList.add(new Result(entireLength - line.length() + matcher.start(1), entireLength - line.length() + matcher.end(1),
                    new OpenFileHyperlinkInfo(myProject, file, lineNumber)));
        }
        return new Result(resultItemList);
    }
}
