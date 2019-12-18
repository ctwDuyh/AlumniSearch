package whu.alumnispider;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import cn.wanghaomiao.xpath.model.JXNode;
import org.jsoup.Connection;
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
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;


import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

        Html html = page.getHtml();


        String url = page.getUrl().toString();
        /*
        Timestamp timestamp = getTime();
        String label = getLabel(html);
        String mainContent = getMainContent(html);
        String briefIntro = getBriefIntro(html);

        int a = alumniDAO.updateContent2(label,mainContent,briefIntro,timestamp,url);
        System.out.println(a);


         */

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

    private String getMainContent(Html html){
        Selectable mainContentPage;
        String mainContentXpath = "//div[@class='main-content']";
        mainContentPage = html.xpath(mainContentXpath);
        String mainContent = mainContentPage.toString();
        /*
        String outOfSummaryPath = "<div class=\"lemma-summary\" label-module=\"lemmaSummary\">" +
                "(?:\\s*?<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>)*\\s*?</div>([\\s\\S]*)";
        String personMainContentPath1 = "<div class=\"para-title level-2\"[\\s\\S]*<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>";
        String personMainContentPath2 = "((\\s*?<div class=\"para\" label-module=\"para\">[\\s\\S]*?<div class=\"lemma-picture text-pic layout-right\"[\\s\\S]*?</div>[\\s\\S]*?</div>)|(\\s*?<div class=\"para\" label-module=\"para\">[\\s\\S]*?</div>))+";
        //String bbbb = "|(\\s*?<div class=\"para\" label-module=\"para\">(?!\\s*?<div)[\\s\\S]*?</div>)";
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

    private static void saveImage(String pictureUrl,String path)  {
        URL url;
        int id;
        id=alumniDAO.insertPictureId(pictureUrl);
        if (id==0){
            id = alumniDAO.getPictureId(pictureUrl);
        }else if (id==-1){
            return;
        }
        try {
            url = new URL(pictureUrl);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path+id+".jpg"));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            alumniDAO.updatePictureSave(id);
        }  catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        //saveImage("https://gss0.bdstatic.com/-4o3dSag_xI4khGkpoWK1HF6hhy/baike/w%3D268%3Bg%3D0/sign=e70649c58544ebf86d716339e1c2b017/8694a4c27d1ed21b82925e23af6eddc450da3f98.jpg","E:/pictures/");

        List<String> pictures = alumniDAO.getPictures();
        for (String picture:pictures){
            saveImage(picture,"E:/pictures/");
        }
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


        /*
        String rgex = "foodd(foodd)*[^foodd]";
        String str = "tomfooddfooddzoofdd";
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(str);
        if (m.find()){
            System.out.println(m.groupCount());
            System.out.println(m.group(0));
            System.out.println(m.group(1));

        }
*/
    }
}
