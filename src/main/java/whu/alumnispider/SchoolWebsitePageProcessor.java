package whu.alumnispider;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import cn.wanghaomiao.xpath.model.JXNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.downloader.BetterDownloader;
import whu.alumnispider.site.MySite;
import whu.alumnispider.tool.HrefTool;
import whu.alumnispider.utilities.School;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  2019-11-11
    Extract URl in Government's website by using BFS.
 */
public class SchoolWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "school";
    private static AlumniDAO alumniDAO = new AlumniDAO();

    private static List<String> collegeUrls = alumniDAO.read("college", "website");
    private static List<String> collegeNames = alumniDAO.read("college","collegename");
    private static List<String> collegeSigs = alumniDAO.read("college", "significant");

    // index represents the elements' index in database.
    private static int maxLevel = 3;


    private static String hrefRegex = "href=.*?>";
    private static Pattern hrefPattern = Pattern.compile(hrefRegex);
    private static String schoolRegex = "edu.cn/?$";
    private static Pattern schoolPattern = Pattern.compile(schoolRegex);
    private static String whuRegex = "武汉大学";
    private static Pattern whuPattern = Pattern.compile(whuRegex);
    private static String hrefXpath = "//a";
    private static String schoolHomeXpath1 = "//a[allText() ~= \'.*院.*\']/@href";
    private static String schoolHomeXpath2 = "//a[allText() ~= \'.*系.*\']/@href";
    private static String schoolHomeXpath3 = "//a[allText() ~= \'.*部.*\']/@href";
    private static String schoolHomeXpath4 = "//a[allText() ~= \'.*机构.*\']/@href";

    static Set<School> extras = new HashSet<School>();

    private Site site = new MySite().site;

    private void schoolPage(Page page)
    {

        String processingUrl = page.getUrl().toString();
        Document document = Jsoup.parse(page.getHtml().toString());
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN(hrefXpath);
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < jxNodes.size(); ++i){
            List<JXNode> hrefNode = new ArrayList<>();
            List<JXNode> textNode = new ArrayList<>();
            try {
                hrefNode = jxNodes.get(i).sel("@href");
                textNode = jxNodes.get(i).sel("allText()");
            } catch (XpathSyntaxErrorException e) {
                e.printStackTrace();
            }

            if(hrefNode.size() == 0) continue;
            else
            {
                String href = hrefNode.get(0).toString();
                String name = "#";
                if (textNode.size()!=0) name = textNode.get(0).toString();

                if (href.charAt(0) == '\"')
                    href = href.substring(1, href.length()-1);

                addSchool(href,name,processingUrl,page);
            }

        }

        Matcher matcher = hrefPattern.matcher(page.getHtml().toString());
        while (matcher.find()) {
            String href = matcher.group();

            href = href.substring(href.indexOf("href="));
            if (href.charAt(5) == '\"') {
                href = href.substring(6);
            } else {
                href = href.substring(5);
            }

            try {
                href = href.substring(0, href.indexOf("\""));
            } catch (Exception e) {
                try {
                    href = href.substring(0, href.indexOf(" "));

                } catch (Exception ee) {
                    href = href.substring(0, href.indexOf(">"));
                }
            }
            addSchool(href, "#", processingUrl, page);
        }

        if((Integer)page.getRequest().getExtra("_level") >= maxLevel) return;

        try {
            jxNodes = jxDocument.selN(schoolHomeXpath1);
            jxNodes.addAll(jxDocument.selN(schoolHomeXpath2));
            jxNodes.addAll(jxDocument.selN(schoolHomeXpath3));
            jxNodes.addAll(jxDocument.selN(schoolHomeXpath4));
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < jxNodes.size(); ++i){
            String href = jxNodes.get(i).toString();
            if (href.charAt(0) == '\"')
                href = href.substring(1, href.length()-1);
            addPage(href,processingUrl,page);
        }
    }

    private void addPage(String href, String processingUrl, Page page)
    {
        if (!href.equals("")&&!href.equals("#")&&!href.contains("javascript")) {
            href = HrefTool.getHref(href, page.getRequest().getExtra("parent").toString(), processingUrl);

            School school = new School(page.getRequest().getExtra("_name").toString(), href);
            if(!extras.contains(school))
            {
                extras.add(school);
                if (school.getWebsite().startsWith(page.getRequest().getExtra("parent").toString())) {
                    Request request = new Request(school.getWebsite()).setPriority(9-(Integer)page.getRequest().getExtra("_level"))
                            .putExtra("_name", school.getCollegeName())
                            .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1))
                            .putExtra("parent", page.getRequest().getExtra("parent"));
                    page.addTargetRequest(request);
                }
            }
        }
    }

    private void addSchool(String href, String name, String processingUrl, Page page)
    {
        if (!href.equals("")&&!href.equals("#")&&!href.startsWith("javascript")) {
            href = HrefTool.getHref(href, page.getRequest().getExtra("parent").toString(), processingUrl);

            School school = new School(page.getRequest().getExtra("_name").toString(), name, href);
            if(!extras.contains(school))
            {
                Matcher schoolMatcher = schoolPattern.matcher(school.getWebsite());
                if(schoolMatcher.find())
                {
                    extras.add(school);
                    alumniDAO.add(school,dataSetName);
                }
            }
        }
    }

    @Override
    public void process(Page page) {
        System.out.print(page.getRequest().getExtra("_level") + " ");
        System.out.print(page.getRequest().getExtra("_name") + " ");
        System.out.println(page.getUrl());

        schoolPage(page);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        /*
        Request request = new Request("https://www.whu.edu.cn/").setPriority(10).putExtra("_level", 0).putExtra("_name", "武汉大学").putExtra("parent", "https://www.whu.edu.cn/");
        Spider spider = Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(request)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(1);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();
        */



        Request[] requests = new Request[2000];
        for(int i = 0; i < 2000; i++)
        {
            requests[i] = new Request(" ");
        }
        //0
        for(int i = 0; i < collegeNames.size(); ++i) {

            if(!collegeSigs.get(i).equals("1"))
            {
                continue;
            }

            String url = collegeUrls.get(i);
            if(url.endsWith(".cn")) url += "/";

            if(!url.startsWith("http")) url = "http://" + url;

            Matcher schoolMatcher = schoolPattern.matcher(url);
            if(!schoolMatcher.find()) {
                continue;
            }

            String name = collegeNames.get(i);
            School school = new School(name, url);
            alumniDAO.add(school, dataSetName);
            extras.add(school);
            Request request = null;

            String parent = url.substring(0,url.indexOf("/", 8)+1);
            if(school.getCollegeName().equals("清华大学"))
            {
                request = new Request("https://www.tsinghua.edu.cn/publish/newthu/newthu_cnt/faculties/index.html").setPriority(10).putExtra("_level", 0).putExtra("_name", name).putExtra("parent", parent);
            }
            else{
                request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", name).putExtra("parent", parent);
            }
            requests[i] = request;
        }
        Spider spider = Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(requests)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(3);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();

    }
}
