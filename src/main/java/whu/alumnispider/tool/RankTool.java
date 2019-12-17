package whu.alumnispider.tool;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankTool {
    public static String[] ranks = new String[]{"院士","长江学者","百人","优青","杰青","千人","万人","青年千人"};

    public static String getRank(String page)
    {
        String rank = "";
        for (String name : ranks)
        {
            Pattern pattern = Pattern.compile(name);
            Matcher matcher = pattern.matcher(page);
            if (matcher.find())
            {
                rank = rank + name + " ";
            }
        }
        return rank;
    }
}
