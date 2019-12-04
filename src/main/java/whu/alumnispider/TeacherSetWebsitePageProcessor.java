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
import whu.alumnispider.utilities.TeacherSet;
import whu.alumnispider.utilities.Website;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeacherSetWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "school";
    private static AlumniDAO alumniDAO = new AlumniDAO();

    private static List<String> schoolUrls = alumniDAO.read("school", "website");
    private static List<String> schoolNames = alumniDAO.read("school","schoolname");

    // index represents the elements' index in database.
    private static int index = 0;
    private static int maxLevel = 3;


    private static String hrefRegex = "<a .*href=.+</a>";
    private static Pattern hrefPattern = Pattern.compile(hrefRegex);
    private static String schoolRegex = "edu.cn/?$";
    private static Pattern schoolPattern = Pattern.compile(schoolRegex);
    private static String whuRegex = "武汉大学";
    private static Pattern whuPattern = Pattern.compile(whuRegex);
    private static String teacherSetXpath1 = "//a[allText() ~= \'.*师.*\']/@href";
    private static String teacherSetXpath2 = "//a[allText() ~= \'.*教.*\']/@href";
    private static String teacherSetXpath3 = "//a[allText() ~= \'.*人.*\']/@href";
    private static String teacherSetXpath4 = "//a[allText() ~= \'.*队.*\']/@href";

    static Set<TeacherSet> extras = new HashSet<TeacherSet>();

    private Site site = new MySite().site;

    private void teacherSetPage(Page page)
    {
        if((Integer)page.getRequest().getExtra("_level") >= maxLevel) return;

        String processingUrl = page.getUrl().toString();

        Document document = Jsoup.parse(page.getHtml().toString());
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN(teacherSetXpath1);
            jxNodes.addAll(jxDocument.selN(teacherSetXpath2));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath3));
            jxNodes.addAll(jxDocument.selN(teacherSetXpath4));
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < jxNodes.size(); ++i){
            String href = jxNodes.get(i).toString();
            if (href.charAt(0) == '\"')
                href = href.substring(1);

            // Delete the extracted empty href.
            addPage(href,processingUrl,page);
        }
    }

    private void addPage(String href, String processingUrl, Page page)
    {
        if (!href.equals("")&&!href.equals("#")&&!href.startsWith("javascript")) {
            if (href.charAt(0) == '.' && processingUrl.endsWith("/")) {
                href = processingUrl + href.substring(2, href.length() - 1);
            } else if (href.charAt(0) == '.') {
                href = processingUrl + href.substring(1, href.length() - 1);
            }

            if(!href.contains("http") && !href.contains("www")) href = page.getRequest().getExtra("parent") + href;

            TeacherSet teacherSet = new TeacherSet(page.getRequest().getExtra("_name").toString(), href);
            if(!extras.contains(teacherSet))
            {
                extras.add(teacherSet);
                alumniDAO.add(teacherSet,dataSetName);
                if (teacherSet.getWebsite().startsWith(page.getRequest().getExtra("parent").toString())) {
                    Request request = new Request(teacherSet.getWebsite()).setPriority(9-(Integer)page.getRequest().getExtra("_level"))
                            .putExtra("_name", teacherSet.getName())
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


        Request[] requests = new Request[4000];
        for(int i = 0; i < 4000; i++)
        {
            requests[i] = new Request(" ");
        }
        while(index < schoolUrls.size()) {

            String url = schoolUrls.get(index);
            if(url.charAt(url.length()-1)!='/') url += "/";

            if(!url.startsWith("http")) url = "http://" + url;

            String name = schoolNames.get(index);
            TeacherSet teacherSet = new TeacherSet(name, url);
            alumniDAO.add(teacherSet, dataSetName);
            extras.add(teacherSet);
            Request request = null;
            request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", name).putExtra("parent", teacherSet.getWebsite());
            requests[index] = request;
            index++;
        }
        Spider spider = Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(requests)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(1);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();
    }
}
