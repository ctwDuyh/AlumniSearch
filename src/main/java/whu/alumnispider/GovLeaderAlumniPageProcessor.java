package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.processor.PageProcessor;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.GovLeaderPerson;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// 2019-11-4
public class GovLeaderAlumniPageProcessor implements PageProcessor {
    private static String govLeaderLinkRe = "/dfzlk/front/personPage\\d+.htm";
    private static String provinceLinkRe = "/dfzlk/front/personProvince\\d{2,}.htm";
    // Note:provinceLinkRe dont contain the seed site.
    private static Pattern provinceLinkPattern = Pattern.compile(provinceLinkRe);
    private static Pattern govLeaderLinkPattern = Pattern.compile(govLeaderLinkRe);

    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "govMunicapleLeader";

    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable provincePage;

        List<String> provincePages = new ArrayList<String>();
        // Below code is the definition of Xpath sentence.
        String provinceLinkXpath = "//div[@class='fl']/ul/li/a/@href";
        //String LeaderLinkXpath = "";
        String processingUrl = page.getUrl().toString();
        Matcher provinceLinkMatcher = provinceLinkPattern.matcher(processingUrl);
        Matcher govLeaderLinkMatcher = govLeaderLinkPattern.matcher(processingUrl);

        if (processingUrl.equals("http://ldzl.people.com.cn/dfzlk/front/personProvince1.htm")) {
            // To get other provinces' pages
            provincePage = page.getHtml().xpath(provinceLinkXpath);
            provincePages = provincePage.all();
            page.addTargetRequests(provincePages);
            GetLeaderPage(page);
        }
        else if (provinceLinkMatcher.find()){
            GetLeaderPage(page);
        }
        else if (govLeaderLinkMatcher.find()) {
            GetLeaderInfo(page);
        }
    }

    public void GetLeaderPage(Page page) {
        /*govLeaderPages stores one page result,
        retPages stores the whole results*/
        List<String> govLeaderPages;
        List<String> retPages = new ArrayList<String>();
        String congressLeaderLinkXpath = "//div[@class='box02']/a/@href";
        String municapleLeaderLinkXpathA = "//ul[@class='list_a']/li/i/a/@href";
        String municapleLeaderLinkXpathB = "//ul[@class='list_a']/li/em/a/@href";

        Selectable congressLeaderPage;
        Selectable municapleLeaderPage;

        congressLeaderPage = page.getHtml().xpath(congressLeaderLinkXpath);
        govLeaderPages = congressLeaderPage.all();
        retPages.addAll(govLeaderPages);
        municapleLeaderPage = page.getHtml().xpath(municapleLeaderLinkXpathA);
        govLeaderPages = municapleLeaderPage.all();
        retPages.addAll(govLeaderPages);
        municapleLeaderPage = page.getHtml().xpath(municapleLeaderLinkXpathB);
        govLeaderPages = municapleLeaderPage.all();
        retPages.addAll(govLeaderPages);

        String govLeaderLink = "";
        Matcher govLeaderLinkMatcher;

        for(int i = 0;i < retPages.size();i++) {
            govLeaderLink = retPages.get(i);
            System.out.println(govLeaderLink);
            //这里不加不对
            govLeaderLink = "http://ldzl.people.com.cn/dfzlk/front/" + govLeaderLink;
            govLeaderLinkMatcher = govLeaderLinkPattern.matcher(govLeaderLink);

            // Filter the url which do not match the LeaderLinkRe.
            if(govLeaderLinkMatcher.find()) {
                govLeaderLink = govLeaderLinkMatcher.group();
                page.addTargetRequest(govLeaderLink);
            }
        }
    }

    public void GetLeaderInfo(Page page) {
        String govLeaderPositionXpath = "//strong[@class='long_name']/text()";
        String govLeaderNameXpayh = "//i[@class='red']/text()";
        String govLeaderCVXpath = "//div[@class='box01']/text()";
        Selectable govLeaderName;
        Selectable govLeaderPosition;
        Selectable govLeaderCV;
        GovLeaderPerson person = new GovLeaderPerson();
        KeywordParser parser = new KeywordParser();

        govLeaderName = page.getHtml().xpath(govLeaderNameXpayh);
        govLeaderPosition = page.getHtml().xpath(govLeaderPositionXpath);
        govLeaderCV = page.getHtml().xpath(govLeaderCVXpath);
        page.putField("govLeaderName", govLeaderName.toString());
        page.putField("govLeaderPosition", govLeaderPosition.toString());
        page.putField("govLeaderCV", govLeaderCV.toString());
        person.setName(govLeaderName.toString());
        person.setJobPosition(govLeaderPosition.toString());
        person.setUrl(page.getUrl().toString());
        // person.setPlaceBirth(govLeaderName.toString());
        // person.setDateBirth(govLeaderName.toString());
        parser.extractor(govLeaderCV.toString(), person);
        new AlumniDAO().add(person, tableName);
        System.out.println(person);
    }

    public static void main(String[] args) {

        Spider.create(new GovLeaderAlumniPageProcessor())
                .addUrl("http://ldzl.people.com.cn/dfzlk/front/personProvince1.htm")
                .thread(3)
                .run();
    }
}

