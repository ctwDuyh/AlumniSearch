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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private static final String[] PERSONILLEGALWORDS = {"涉嫌严重","因严重"};


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
        }
        if (isPersonRelated2Whu(page)){
            getInformation(page);
        }
        //已经爬取完
        if (searchNameList.contains(name)){
            alumniDAO.updateCandidate(name);
        }

    }

    private boolean isPersonRelated2Whu(Page page){

        String entryTextXpath = "//dd/allText()";
        String mainTextXpath = "//div[@class='para']/allText()";
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

    private boolean isPersonRelated2Illegal(Html html){
        String mainTextXpath = "//div[@class='para']/allText()";
        Selectable personWord;
        List<String> personWords;
        personWord = html.xpath(mainTextXpath);
        personWords = personWord.all();
        return isWordRelated2Illegal(personWords);
    }

    private boolean isWordRelated2Whu(List<String> personWords){
        for (String schoolName : SCHOOLNAME){
            for (String word : personWords)
                if (word.contains(schoolName))
                    return true;
        }
        return false;
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

        Alumni person = new Alumni();
        Html html = page.getHtml();
        personName = getPersonName(html);
        personJob = getPersonJob(html);
        isIllegal = isPersonRelated2Illegal(html);
        website = page.getUrl().toString();
        picture = getPersonPicture(html);
        content = getPersonContent(html);

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
        searchNameList = alumniDAO.getCandidate();

        List<String> urls = new ArrayList<>();
        for (String name : searchNameList){
            urls.add("https://baike.baidu.com/item/"+name);
        }
        String[] urlArray = new String[urls.size()];
        urls.toArray(urlArray);
        Spider.create(new BaiduSearchProcessor())
                .addUrl(urlArray)
                .thread(3)
                .run();

    }
}
