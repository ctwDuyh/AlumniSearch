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
import whu.alumnispider.tool.UrlTool;
import whu.alumnispider.utilities.Teacher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "teacher";
    private static AlumniDAO alumniDAO = new AlumniDAO();

    private static List<String> teacherSetUrls = alumniDAO.read("teacherset", "website");
    private static List<String> collegeNames = alumniDAO.read("teacherset","collegename");
    private static List<String> schoolNames = alumniDAO.read("teacherset","schoolname");
    private static List<String> teacherSetNames = alumniDAO.read("teacherset","teachersetname");

    private static String teacherSetXpath1 = "//a[allText() ~= \'.*师资.*\']";
    private static String teacherSetXpath2 = "//a[allText() ~= \'.*教师.*\']";
    private static String teacherSetXpath3 = "//a[allText() ~= \'.*人物.*\']";
    private static String teacherSetXpath4 = "//a[allText() ~= \'.*队伍.*\']";
    private static String teacherSetXpath5 = "//a[allText() ~= \'.*系列.*\']";
    private static String teacherSetXpath6 = "//a[allText() ~= \'.*人员.*\']";
    private static String teacherSetXpath7 = "//a[allText() ~= \'.*教员.*\']";
    private static String teacherSetXpath8 = "//a[allText() ~= \'.*教授.*\']";
    private static String teacherSetXpath9 = "//a[allText() ~= \'.*博士.*\']";
    private static String teacherSetXpath14 = "//a[allText() ~= \'.*研究.*\']";
    private static String teacherSetXpath15 = "//a[allText() ~= \'.*学科.*\']";
    private static String teacherSetXpath16 = "//a[allText() ~= \'.*人才.*\']";
    private static String teacherSetXpath10 = "//a[allText() ~= \'.*院.*\']/@href";
    private static String teacherSetXpath11 = "//a[allText() ~= \'.*系.*\']/@href";
    private static String teacherSetXpath12 = "//a[allText() ~= \'.*部.*\']/@href";
    private static String teacherSetXpath13 = "//a[allText() ~= \'.*所.*\']/@href";

    private static String teacherXpath = "//a";
    private static int maxLevel = 1;


    static Set<Teacher> extras = new HashSet<Teacher>();
    static Set<Teacher> teachers = new HashSet<Teacher>();

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
            jxNodes.addAll(jxDocument.selN(teacherSetXpath10));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath11));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath12));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath13));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath14));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath15));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath16));
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
                addPage(href,name,processingUrl,page,false);
            }
        }
    }

    private void teacherPage(Page page)
    {
        String processingUrl = page.getUrl().toString();

        Document document = Jsoup.parse(page.getHtml().toString());
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN(teacherXpath);
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
                addPage(href,name,processingUrl,page,true);
            }
        }
    }

    private void addPage(String href, String name, String processingUrl, Page page, boolean addTeacher)
    {
        if (!href.equals("")&&!href.equals("#")&&!href.contains("javascript")) {
            href = HrefTool.getHref(href, page.getRequest().getExtra("parent").toString(), processingUrl);

            Teacher teacher = new Teacher(page.getRequest().getExtra("_name").toString(),
                    page.getRequest().getExtra("_sname").toString(), page.getRequest().getExtra("_tname").toString(), name, href);
            if(!extras.contains(teacher)&&!addTeacher)
            {
                extras.add(teacher);
                if(!teacher.getWebsite().startsWith(page.getUrl().toString().substring(0,page.getUrl().toString().indexOf(".edu.cn")+7))) return;
                if((Integer)page.getRequest().getExtra("_level")<maxLevel && !addTeacher)
                {
                    Request request = new Request(teacher.getWebsite()).setPriority(9-(Integer)page.getRequest().getExtra("_level"))
                            .putExtra("_name", teacher.getCollegeName())
                            .putExtra("_sname", teacher.getSchoolName())
                            .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1))
                            .putExtra("_tname", teacher.getTeacherName())
                            .putExtra("parent", page.getRequest().getExtra("parent"));
                    page.addTargetRequest(request);
                }
            }
            if(!teachers.contains(teacher)&&addTeacher)
            {
                teachers.add(teacher);
                alumniDAO.add(teacher,dataSetName);
            }
        }
    }

    @Override
    public void process(Page page) {
        System.out.print(page.getRequest().getExtra("_level") + " ");
        System.out.print(page.getRequest().getExtra("_name") + " ");
        System.out.print(page.getRequest().getExtra("_sname") + " ");
        System.out.println(page.getUrl());

        teacherPage(page);

        if((Integer)page.getRequest().getExtra("_level") < maxLevel)
        {
            teacherSetPage(page);
        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        /*
        Request request = new Request("https://yyxy.hunnu.edu.cn/xygk/szdw.htm").setPriority(10).putExtra("_level", 0).putExtra("_name", "武汉大学")
        .putExtra("_sname", "武汉大学").putExtra("_tname", "武汉大学").putExtra("parent", "https://www.whu.edu.cn/");
        Spider spider = Spider.create(new TeacherWebsitePageProcessor())
                .addRequest(request)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(1);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();

        */



        Request[] requests = new Request[20000];
        for(int i = 0; i < 20000; i++)
        {
            requests[i] = new Request(" ");
        }

        //10000

        for(int i = 10000; i < teacherSetUrls.size(); ++i) {

            String url = teacherSetUrls.get(i);
            url = UrlTool.getPreparedUrl(url);

            String cname = collegeNames.get(i);
            String sname = schoolNames.get(i);
            String tname = teacherSetNames.get(i);
            Teacher teacher = new Teacher(cname, sname, tname, url);
            extras.add(teacher);
            Request request = null;
            String parent = url.substring(0,url.indexOf("/", 8)+1);
            request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", cname).putExtra("parent", parent).putExtra("_sname", sname).putExtra("_tname", tname);
            requests[i] = request;
        }

        Spider spider = Spider.create(new TeacherWebsitePageProcessor())
                .addRequest(requests)
                .thread(3);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();

    }
}
