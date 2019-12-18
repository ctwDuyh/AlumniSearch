package whu.alumnispider;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import cn.wanghaomiao.xpath.model.JXNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduSearchProcessor implements PageProcessor {
    private static AlumniDAO alumniDAO = new AlumniDAO();
    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "alumnis";
    private static List<String> searchNameList;
    private static final String[] SCHOOLNAME = {"武汉大学","武汉水利电力大学","武汉测绘科技大学","湖北医科大学",
            "武汉水利水电学院", "葛洲坝水电工程学院","武汉测绘学院","武汉测量制图学院","湖北医学院","湖北省医学院",
            "湖北省立医学院","武汉水利电力学院"};
    private static final String[] ILLEGALWORDS = {"违纪","违法"};
    private static final String[] PERSONILLEGALWORDS = {"涉嫌","因"};


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
        String personNamePath = "https://baike\\.baidu\\.com/item/(.*)";
        String name = getMatching(page.getUrl().toString(),personNamePath);
        // 第一次搜索，才爬取其他目录的网址
        if (searchNameList.contains(name)){
            // 寻找其他目录
            personPage = page.getHtml().xpath(personLinkXpath);
            personPages = personPage.all();
            for(String tempPage : personPages){
                Request request = new Request(tempPage);
                page.addTargetRequest(request);
            }
            personPage = page.getHtml().xpath(personLinkXpath2);
            personPages = personPage.all();
            for(String tempPage : personPages){
                tempPage = tempPage + "#viewPageContent";
                Request request = new Request(tempPage);
                page.addTargetRequest(request);
            }
            // 该页面不用爬取
            if (personPages.size()>0)
                return;
        }
        if (isPersonRelated2Whu(page)){
            getInformation(page);
        }
        //已经爬取完
        if (searchNameList.contains(name)){
            alumniDAO.updateCandidate(name);
        }

    }
    // 判断人物是否与武大有关，并保存网址和匹配的关键词
    private boolean isPersonRelated2Whu(Page page){

        String entryTextXpath = "//dd/allText()";
        String mainTextXpath = "//div[@class='para']/allText()";
        // 寻找当前目录是否与武大有关
        Selectable personWord;
        List<String> personWords = new ArrayList<String>();
        String schoolName;
        String url = page.getUrl().toString();
        // 先检索词条信息
        personWord = page.getHtml().xpath(entryTextXpath);
        personWords = personWord.all();
        schoolName = getWordRelated2Whu(personWords);
        if (schoolName!=null){
            //System.out.println("人物词条匹配成功");
            System.out.println("匹配成功的网址为:"+url);
            alumniDAO.addWebsite(url,schoolName);
            return true;
        }
        else{
            // 词条不匹配，再检索人物的主要信息
            personWord = page.getHtml().xpath(mainTextXpath);
            personWords = personWord.all();
            schoolName = getWordRelated2Whu(personWords);
            if (schoolName!=null){
                //System.out.println("人物主要内容匹配成功");
                System.out.println("匹配成功的网址为:"+url);
                alumniDAO.addWebsite(url,schoolName);
                return true;
            }
        }
        alumniDAO.addWebsite(url, null);
        return false;
    }

    private boolean isPersonRelated2Illegal(Html html){
        String mainTextXpath = "//div[@class='para']/allText()";
        Selectable personWord;
        List<String> personWords;
        personWord = html.xpath(mainTextXpath);
        personWords = personWord.all();
        return isWordRelated2Illegal(personWords);
    }

    private String getWordRelated2Whu(List<String> personWords){
        for (String schoolName : SCHOOLNAME){
            for (String word : personWords)
                if (word.contains(schoolName))
                    return schoolName;
        }
        return null;
    }

    private boolean isWordRelated2Illegal(List<String> personWords){
        for (String illegalWord : ILLEGALWORDS){
            for (String word : personWords)
                if (word.contains(illegalWord)){
                    for (String personIllegalWord : PERSONILLEGALWORDS){
                        if (word.contains(personIllegalWord))
                            return true;
                    }
                }
        }
        return false;
    }

    // get person information
    private void getInformation(Page page){

        String personJob;
        String personName;
        boolean isIllegal = false;
        String website;
        String picture;
        String content;
        String label;
        String briefIntro;
        String mainContent;
        Timestamp time;

        Alumni person = new Alumni();
        Html html = page.getHtml();
        personName = getPersonName(html);
        personJob = getPersonJob(html);
        isIllegal = isPersonRelated2Illegal(html);
        website = page.getUrl().toString();
        picture = getPersonPicture(html);
        content = getPersonContent(html);
        label = getLabel(html);
        briefIntro = getBriefIntro(html);
        mainContent = getMainContent(html);
        time = getTime();

        System.out.println("人物姓名："+personName+", 人物信息："+personJob+", 人物图片："+picture);
        if (isIllegal){
            System.out.println("注意："+personName+"涉嫌违法违纪。");
        }
        person.setName(personName);
        person.setJob(personJob);
        person.setIllegal(isIllegal);
        person.setWebsite(website);
        person.setPicture(picture);
        person.setContent(content);
        person.setLabel(label);
        person.setMainContent(mainContent);
        person.setBriefIntro(briefIntro);
        person.setTime(time);


        alumniDAO.add(person,tableName);
    }

    private String getPersonName(Html html){
        String personNamePath = "//dd[@class='lemmaWgt-lemmaTitle-title']/h1/text()";
        Selectable personNamePage;
        String personName;
        personNamePage = html.xpath(personNamePath);
        personName = personNamePage.toString();
        return personName;
    }

    private String getPersonPicture(Html html){
        String personPicturePath = "//div[@class='summary-pic']/a/img/@src";
        Selectable personPicturePage;
        List<String> personPictures;
        personPicturePage = html.xpath(personPicturePath);
        personPictures = personPicturePage.all();
        for (String personPicture : personPictures){
            return personPicture;
        }
        return null;
    }


    private String getPersonJob(Html html){
        // xpath
        String personJobInfoPath1 = "//dd[@class='lemmaWgt-lemmaTitle-title']/h2/text()";
        String personJobInfoPath2 = "//div[@class='para']/allText()";
        String personJobInfoPath3 = "//div[@class='basic-info cmn-clearfix']/allText()";
        // java rgex
        String[] personJobPath2s = {"现任(.*?)。","职业为(.*?)。","现为(.*?)。","现系(.*?)。","现任(.*?)；","职业为(.*?)；","现为(.*?)；","现系(.*?)；"};
        String[] personJobPath3 = {"职务 (.*?) ","职称 (.*?) ","职业 (.*?) "};
        String personJobPath1 = "（(.*)）";
        String indexPath = "\\[\\d*?\\-?\\d*?\\]";
        String blankPath = "(\\s|\\u00A0)*";
        String blank160Path = "\\u00A0*";

        Selectable personJobInfoPage;
        String personJob;
        List<String> personJobInfos = new ArrayList<>();

        // 从匹配模式二获取职业信息
        personJobInfoPage = html.xpath(personJobInfoPath2);
        personJobInfos = personJobInfoPage.all();
        personJob = getMatching(personJobInfos,personJobPath2s);
        if (personJob==null){
            // 从匹配模式一种获取职业信息
            personJobInfoPage = html.xpath(personJobInfoPath1);
            personJob = personJobInfoPage.toString();
            // 判断从匹配模式一是否能够获取职业信息
            if (personJob!=null){
                //java正则匹配去除结果中括号
                personJob = getMatching(personJob,personJobPath1);
            }
            //前两个模式都匹配失败，尝试从匹配模式三种获取职业信息
            else {
                personJobInfoPage = html.xpath(personJobInfoPath3);
                personJob = personJobInfoPage.toString();
                //结尾加空格，使得dd的内容始终被空格包裹
                personJob = personJob + " ";
                personJob = personJob.replaceAll(blank160Path,"");
                personJob = getMatching(personJob,personJobPath3);
            }
        }
        if (personJob!=null){
            personJob = personJob.replaceAll(indexPath,"");
            personJob = personJob.replaceAll(blankPath,"");
        }
        return personJob;
    }

    private String getPersonContent(Html html) {
        String content = "";
        String contentXpath1="//div[@class='para']/allText()";
        String contentXpath2="//dd[@class='lemmaWgt-lemmaTitle-title']/allText()";
        String contentXpath3="//div[@class='basic-info cmn-clearfix']//dd/allText()";
        String contentXpath4="//dl[@class='lemma-reference collapse nslog-area log-set-param']//li/allText()";
        List<String> contents = new ArrayList<>();
        Selectable contentPage;
        contentPage = html.xpath(contentXpath1);
        contents = contentPage.all();
        contentPage = html.xpath(contentXpath2);
        contents.add(contentPage.toString());
        contentPage = html.xpath(contentXpath3);
        contents.addAll(contentPage.all());
        contentPage = html.xpath(contentXpath4);
        contents.addAll(contentPage.all());
        for(String tempContent : contents){
            for (String schoolName : SCHOOLNAME){
                if (tempContent.contains(schoolName)){
                    content = content + tempContent;
                }
            }
        }
        return content;
    }

    private String getLabel(Html html){
        Selectable labelPage;
        String label;
        String labelXpath = "//div[@id='open-tag']/dd[@id='open-tag-item']/allText()";

        labelPage = html.xpath(labelXpath);
        label = labelPage.toString();
        return label;
    }

    private Timestamp getTime(){
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    private String getBriefIntro(Html html){
        Selectable briefIntroPage;
        String briefIntro = "";
        List<String> briefIntroList;
        String briefIntroXpath="//div[@class='lemma-summary']/div[@class='para']/allText()";

        briefIntroPage = html.xpath(briefIntroXpath);
        briefIntroList = briefIntroPage.all();
        for (String str : briefIntroList){
            briefIntro = briefIntro + str;
        }
        return briefIntro;
    }

    private String getMainContent(Html html){
        Selectable mainContentPage;
        String mainContentXpath = "//div[@class='main-content']";
        mainContentPage = html.xpath(mainContentXpath);
        String mainContent = mainContentPage.toString();
        /*
        String outOfSummaryPath = "<div class=\"lemma-summary\" label-module=\"lemmaSummary\">" +
                "[\\s\\S]*?</div>\\s*?</div>([\\s\\S]*)";
        String personMainContentPath1 = "<div class=\"para-title level-2\"[\\s\\S]*<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>";
        String personMainContentPath2 = "<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>(?:<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>)*";

        Pattern outOfSumPattern = Pattern.compile(outOfSummaryPath);
        Pattern mainContentPattern1 = Pattern.compile(personMainContentPath1);
        Pattern mainContentPattern2 = Pattern.compile(personMainContentPath2);

        Matcher m = outOfSumPattern.matcher(mainContent);
        // 去除简介
        if (m.find()){
            mainContent = m.group(1);
        }
        // 获取正文
        m = mainContentPattern1.matcher(mainContent);
        if (m.find()){
            mainContent = m.group(0);
        }else {
            m = mainContentPattern2.matcher(mainContent);
            if (m.find()){
                mainContent = m.group(0);
            }
        }
        // 转换成document
        Document document = Jsoup.parse(mainContent);
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN("./allText()");
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
        mainContent = jxNodes.get(0).toString();
        */
        return mainContent;
    }

    // 用于职业匹配模式三
    private String getMatching(String soap,String[] rgexs){
        for (String rgex : rgexs){
            Pattern pattern = Pattern.compile(rgex);
            Matcher m = pattern.matcher(soap);
            if(m.find()){
                return m.group(1);
            }
        }
        return null;
    }

    // 用于职业匹配模式二
    private String getMatching(List<String> soaps,String[] rgexs){
        for (String soap : soaps){
            for (String rgex : rgexs){
                Pattern pattern = Pattern.compile(rgex);
                Matcher m = pattern.matcher(soap);
                if(m.find()){
                    return m.group(1);
                }
            }
        }
        return null;
    }

    // 用于职业匹配模式一
    private String getMatching(String soap,String rgex){
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        if(m.find()){
            return m.group(1);
        }
        return soap;
    }


    public static void main(String[] args) {

        //searchNameList = Arrays.asList("张德明");
        /*
        searchNameList = alumniDAO.getCandidate();

        List<String> urls = new ArrayList<>();
        for (String name : searchNameList){
            urls.add("https://baike.baidu.com/item/"+name);
        }
        String[] urlArray = new String[urls.size()];
        urls.toArray(urlArray);
        */

        Spider.create(new BaiduSearchProcessor())
                .addUrl("https://baike.baidu.com/item/%E5%88%98%E4%BC%A0%E9%93%81")
                .thread(3)
                .run();

    }
}
