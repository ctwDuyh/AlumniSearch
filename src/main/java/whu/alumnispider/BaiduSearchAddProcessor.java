package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.Alumni;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduSearchAddProcessor implements PageProcessor {
    private static AlumniDAO alumniDAO = new AlumniDAO();
    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
    private static List<String> searchWebsiteList;
    private static final String[] SCHOOLNAME = {"武汉大学","武汉水利电力大学","武汉测绘科技大学","湖北医科大学",
            "武汉水利水电学院", "葛洲坝水电工程学院","武汉测绘学院","武汉测量制图学院","湖北医学院","湖北省医学院",
            "湖北省立医学院","武汉水利电力学院"};

    @Override
    public void process(Page page) {
        String content = "";
        String contentXpath1="//div[@class='para']/allText()";
        String contentXpath2="//dd[@class='lemmaWgt-lemmaTitle-title']/allText()";
        String contentXpath3="//div[@class='basic-info cmn-clearfix']//dd/allText()";
        String contentXpath4="//dl[@class='lemma-reference collapse nslog-area log-set-param']//li/allText()";
        List<String> contents = new ArrayList<>();
        Selectable contentPage;
        contentPage = page.getHtml().xpath(contentXpath1);
        contents = contentPage.all();
        contentPage = page.getHtml().xpath(contentXpath2);
        contents.add(contentPage.toString());
        contentPage = page.getHtml().xpath(contentXpath3);
        contents.addAll(contentPage.all());
        contentPage = page.getHtml().xpath(contentXpath4);
        contents.addAll(contentPage.all());
        for(String tempContent : contents){
            for (String schoolName : SCHOOLNAME){
                if (tempContent.contains(schoolName)){
                    content = content + tempContent;
                }
            }
        }
        alumniDAO.updateContent(page.getUrl().toString(),content);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        /*
        searchWebsiteList = alumniDAO.getWebsite();

        List<String> urls = new ArrayList<>(searchWebsiteList);
        String[] urlArray = new String[urls.size()];
        urls.toArray(urlArray);
        Spider.create(new BaiduSearchAddProcessor())
                .addUrl(urlArray)
                .thread(3)
                .run();
        */
        String rgex = "(z|f)ood";
        String str = "zood1";
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(str);
        if (m.find()){
            System.out.println(m.groupCount());
            System.out.println(m.group(0));
            System.out.println(m.group(1));

        }

    }
}
