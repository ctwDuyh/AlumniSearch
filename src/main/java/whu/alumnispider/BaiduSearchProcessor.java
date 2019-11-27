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
    //private static String personLinkRe = "https://baike.baidu.com/item/";
    //private static Pattern personLinkPattern = Pattern.compile(personLinkRe);
    private static String searchName;
    private boolean firstSearch = true;

    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "alumni";

    private static final String[] SCHOOLNAME = {"武汉大学","武汉水利电力大学","武汉测绘科技大学","湖北医科大学",
            "湖北水利水电学院", "葛洲坝水电工程学院","武汉测绘学院","武汉测量制图学院","湖北医学院","湖北省医学院","湖北省立医学院"};
    private HashSet<String> personHashSet = new HashSet<>();


    @Override
    public Site getSite() {
        return site;
    }


    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable personPage;

        List<String> personPages = new ArrayList<String>();

        // Below code is the definition of Xpath sentence.
        String personLinkXpath = "//ul[@class='polysemantList-wrapper cmn-clearfix']/li/a/@href";
        String personLinkXpath2 = "//ul[@class='custom_dot  para-list list-paddingleft-1']/li/div/a/@href";

        //String processingUrl = page.getUrl().toString();
        //Matcher personLinkMatcher = personLinkPattern.matcher(processingUrl);

        if (firstSearch) {
            firstSearch = false;
            //寻找其他目录
            personPage = page.getHtml().xpath(personLinkXpath);
            personPages = personPage.all();
            for(String tempPage : personPages){
                if (!personHashSet.contains(tempPage)){
                    personHashSet.add(tempPage);
                    Request request = new Request(tempPage);
                    page.addTargetRequest(request);
                }
            }
            personPage = page.getHtml().xpath(personLinkXpath2);
            personPages = personPage.all();
            for(String tempPage : personPages){
                tempPage = tempPage + "#viewPageContent";
                if (!personHashSet.contains(tempPage)){
                    personHashSet.add(tempPage);
                    Request request = new Request(tempPage);
                    page.addTargetRequest(request);
                }
            }
        }
        if (isPersonRelated2Whu(page)){
            getInformation(page);
        }


    }
    //
    private boolean isPersonRelated2Whu(Page page){
        String entryTextXpath = "//dd/text()";
        String mainTextXpath = "//div[@class='para']/text()";
        // 寻找当前目录是否与武大有关
        Selectable personWord;
        List<String> personWords = new ArrayList<String>();
        // 先检索词条信息
        personWord = page.getHtml().xpath(entryTextXpath);
        personWords = personWord.all();
        if (isWordRelated2Whu(personWords)){
            // test
            //System.out.println("人物词条匹配成功");
            String url = page.getUrl().toString();
            System.out.println("匹配成功的网址为:"+url);
            return true;
        }
        else{
            // 词条不匹配，再检索人物的主要信息
            personWord = page.getHtml().xpath(mainTextXpath);
            personWords = personWord.all();
            if (isWordRelated2Whu(personWords)){
                //System.out.println("人物主要内容匹配成功");
                String url = page.getUrl().toString();
                System.out.println("匹配成功的网址为:"+url);
                return true;
            }
        }
        return false;
    }

    private boolean isWordRelated2Whu(List<String> personWords){
        for (String schoolName : SCHOOLNAME){
            for (String word : personWords)
                if (word.contains(schoolName))
                    return true;
        }
        return false;
    }
    // get person job(null is possible)
    private void getInformation(Page page){
        String infoTextPath = "//dd[@class='lemmaWgt-lemmaTitle-title']/h2/text()";
        Selectable personJobInfoPage;
        personJobInfoPage = page.getHtml().xpath(infoTextPath);
        String personJobInfo = personJobInfoPage.toString();
        System.out.println("人物姓名："+searchName+" , 人物信息："+personJobInfo);
    }

    public static void main(String[] args) {
        searchName = "杨志坚";
        Spider.create(new BaiduSearchProcessor())
                .addUrl("https://baike.baidu.com/item/"+searchName)
                .thread(3)
                .run();

    }
}
