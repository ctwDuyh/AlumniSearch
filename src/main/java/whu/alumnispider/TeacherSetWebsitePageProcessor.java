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
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.downloader.BetterDownloader;
import whu.alumnispider.site.MySite;
import whu.alumnispider.tool.HrefTool;
import whu.alumnispider.utilities.TeacherSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TeacherSetWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "teacherset";
    private static AlumniDAO alumniDAO = new AlumniDAO();

    private static List<String> schoolUrls = alumniDAO.read("school", "website");
    private static List<String> collegeNames = alumniDAO.read("school","collegename");
    private static List<String> schoolNames = alumniDAO.read("school","schoolname");

    // index represents the elements' index in database.
    private static int maxLevel = 2;


    private static String hrefRegex = "<a .*href=.+</a>";
    private static Pattern hrefPattern = Pattern.compile(hrefRegex);
    private static String schoolRegex = "edu.cn/?$";
    private static Pattern schoolPattern = Pattern.compile(schoolRegex);
    private static String whuRegex = "武汉大学";
    private static Pattern whuPattern = Pattern.compile(whuRegex);
    private static String teacherSetXpath1 = "//a[allText() ~= \'.*师资.*\']";
    private static String teacherSetXpath2 = "//a[allText() ~= \'.*教师.*\']";
    private static String teacherSetXpath3 = "//a[allText() ~= \'.*人物.*\']";
    private static String teacherSetXpath4 = "//a[allText() ~= \'.*队伍.*\']";
    private static String teacherSetXpath5 = "//a[allText() ~= \'.*系列.*\']";
    private static String teacherSetXpath6 = "//a[allText() ~= \'.*人员.*\']";
    private static String teacherSetXpath7 = "//a[allText() ~= \'.*教员.*\']";
    private static String teacherSetXpath8 = "//a[allText() ~= \'.*教授.*\']";
    private static String teacherSetXpath9 = "//a[allText() ~= \'.*博士.*\']";

    static Set<TeacherSet> extras = new HashSet<TeacherSet>();

    private Site site = new MySite().site;

    private void teacherSetPage(Page page)
    {
        String processingUrl = page.getUrl().toString();

        Document document = Jsoup.parse(page.getHtml().toString());
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN(teacherSetXpath1);
            jxNodes.addAll(jxDocument.selN(teacherSetXpath2));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath3));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath4));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath5));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath6));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath7));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath8));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath9));
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
                if (href.charAt(0) == '\"')
                href = href.substring(1, href.length()-1);

                if (textNode.size()!=0) name = textNode.get(0).toString();
                // Delete the extracted empty href.
                addPage(href,name,processingUrl,page);
            }
        }
    }

    private void addPage(String href, String name, String processingUrl, Page page)
    {
        if (!href.equals("")&&!href.equals("#")&&!href.contains("javascript")) {
            href = HrefTool.getHref(href, page.getRequest().getExtra("parent").toString(), processingUrl);

            TeacherSet teacherSet = new TeacherSet(page.getRequest().getExtra("_name").toString(),
                    page.getRequest().getExtra("_sname").toString(), name, href);
            if(!extras.contains(teacherSet))
            {
                extras.add(teacherSet);
                alumniDAO.add(teacherSet,dataSetName);
                if((Integer)page.getRequest().getExtra("_level") >= maxLevel) return;
                if (teacherSet.getWebsite().startsWith(page.getRequest().getExtra("parent").toString())) {
                    Request request = new Request(teacherSet.getWebsite()).setPriority(9-(Integer)page.getRequest().getExtra("_level"))
                            .putExtra("_name", teacherSet.getCollegeName())
                            .putExtra("_sname", teacherSet.getSchoolName())
                            .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1))
                            .putExtra("parent", page.getRequest().getExtra("parent"));
                    page.addTargetRequest(request);
                }
            }
        }
    }

    @Override
    public void process(Page page) {
        System.out.print(page.getRequest().getExtra("_level") + " ");
        System.out.print(page.getRequest().getExtra("_name") + " ");
        System.out.print(page.getRequest().getExtra("_sname") + " ");
        System.out.println(page.getUrl());

        teacherSetPage(page);

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


        Request[] requests = new Request[8800];
        for(int i = 0; i < 8800; i++)
        {
            requests[i] = new Request(" ");
        }
        //over

        for(int i = 8000; i < 8800; ++i) {

            String url = schoolUrls.get(i);
            if(url.endsWith(".cn")) url += "/";

            if(!url.startsWith("http")) url = "http://" + url;

            String cname = collegeNames.get(i);
            String sname = schoolNames.get(i);
            TeacherSet teacherSet = new TeacherSet(cname, sname, url);
            extras.add(teacherSet);
            Request request = null;
            String parent = url.substring(0,url.indexOf("/", 8)+1);
            request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", cname).putExtra("parent", parent).putExtra("_sname", sname);
            requests[i] = request;
        }
        Spider spider = Spider.create(new TeacherSetWebsitePageProcessor())
                .addRequest(requests)
                .thread(3);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();
    }
}
