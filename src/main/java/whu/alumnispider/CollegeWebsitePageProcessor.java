package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// 2019-11-4
public class CollegeWebsitePageProcessor implements PageProcessor {
    private static String homeLinkRe = "http://college.gaokao.com/schlist/p\\d+/";
    private static String collegeLinkRe = "http://college.gaokao.com/school/\\d+/";
    private static Pattern homeLinkPattern = Pattern.compile(homeLinkRe);
    private static Pattern collegeLinkPattern = Pattern.compile(collegeLinkRe);

    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "college";

    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable collegePage;

        List<String> collegePages = new ArrayList<String>();
        // Below code is the definition of Xpath sentence.
        String collegeLinkXpath = "//div[@class='scores_List']/dl/dt/a/@href";
        String processingUrl = page.getUrl().toString();
        Matcher homeLinkMatcher = homeLinkPattern.matcher(processingUrl);
        Matcher collegeLinkMatcher = collegeLinkPattern.matcher(processingUrl);

        if (homeLinkMatcher.find()) {
            // To get colleges' pages
            collegePage = page.getHtml().xpath(collegeLinkXpath);
            collegePages = collegePage.all();
            page.addTargetRequests(collegePages);
        }
        else if(collegeLinkMatcher.find())
        {
            getCollegePage(page);
        }
    }

    public void getCollegePage(Page page) {
        String collegeNameXpath = "//div[@class='bg_sez']/h2/text()";
        String collegeContentXpath = "//div[@class='college_msg bk']/dl/dd/ul[@class='left contact']";
        Selectable collegeName;
        Selectable collegeContent;
        College college = new College();

        collegeName = page.getHtml().xpath(collegeNameXpath);
        collegeContent = page.getHtml().xpath(collegeContentXpath);

        college.setName(collegeName.toString());
        String content = collegeContent.toString();
        int start = content.indexOf("学校网址：");
        int end = content.indexOf(".cn");
        if(start==-1||end==-1) return;
        String website = content.substring(start+5,end+3);
        String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则
        website = website.replaceAll(REGEX_CHINESE, "");
        college.setWebsite(website);

        new AlumniDAO().add(college, tableName);
    }

    public static void main(String[] args) {
        String strings[] = new String[150];
        for(int i = 0; i < 150; ++i)
        {
            strings[i] = "http://college.gaokao.com/schlist/p" + (i+1) + "/";
        }
        Spider.create(new CollegeWebsitePageProcessor())
                .addUrl(strings)
                .thread(3)
                .run();
    }
}

