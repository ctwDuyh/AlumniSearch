package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduSearchProcessor implements PageProcessor {
    private static String personLinkRe = "https://baike.baidu.com/item/\\S+";
    private static Pattern personLinkPattern = Pattern.compile(personLinkRe);

    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "alumni";

    public HashSet<String> personHashSet = new HashSet<>();
    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable personPage;

        List<String> personPages = new ArrayList<String>();
        // Below code is the definition of Xpath sentence.
        String collegeLinkXpath = "//ul[@class='polysemantList-wrapper cmn-clearfix']/li/a/@href";
        String processingUrl = page.getUrl().toString();
        Matcher homeLinkMatcher = personLinkPattern.matcher(processingUrl);

        if (homeLinkMatcher.find()) {
            // To get colleges' pages
            personPage = page.getHtml().xpath(collegeLinkXpath);
            personPages = personPage.all();
            for(String tempPage : personPages){
                if (!personHashSet.contains(tempPage)){
                    personHashSet.add(tempPage);
                    Request request = new Request(tempPage);
                    page.addTargetRequest(request);
                }
            }
        }

    }

    @Override
    public Site getSite() {
        return null;
    }

    public static void main(String[] args) {

        Spider.create(new CollegeWebsitePageProcessor())
                .addUrl("https://baike.baidu.com/item/陈文光")
                .thread(3)
                .run();
    }
}
