package whu.alumnispider.tool;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WHUTool {
    public static String[] whu = new String[]{"武汉大学","武大","武汉水利电力大学","武汉测绘科技大学","湖北医科大学","武汉水利电力学院"
            ,"葛洲坝水电工程学院","武汉测绘学院","武汉测量值图学院","湖北医学院","湖北省医学院","湖北省立医学院","武汉水利水电学院"};
    public static String[] graduate = new String[]{"本科","学士","硕士","博士","学位","毕业","研究生"};

    public static boolean isWHU(String s)
    {
        ArrayList<Integer> indexs = new ArrayList<>();
        for (String name : whu)
        {
            Pattern pattern = Pattern.compile(name);
            Matcher matcher = pattern.matcher(s);
            while (matcher.find())
            {
                indexs.add(matcher.start());
            }
        }
        for (int i : indexs)
        {
            String text = s.substring(i-20, i+30);
            for (String name : graduate)
            {
                if (text.contains(name)) return true;
            }
        }
        return false;
    }

}
