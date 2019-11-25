package whu.alumnispider.parser;


import whu.alumnispider.utilities.Person;
import whu.alumnispider.utilities.ReExpUtility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordParser {
    private static Pattern WhuPattern = Pattern.compile(ReExpUtility.reWuhanUniversity);

    public void extractor(String CV, Person person) {
        Matcher whuMatcher = WhuPattern.matcher(CV);
        if(whuMatcher.find())
            person.setGraduatedSchool("武汉大学");
    }
}
