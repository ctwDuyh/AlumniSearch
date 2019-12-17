package whu.alumnispider;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import cn.wanghaomiao.xpath.model.JXNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.downloader.BetterDownloader;
import whu.alumnispider.site.MySite;
import whu.alumnispider.tool.RankTool;
import whu.alumnispider.tool.WHUTool;
import whu.alumnispider.utilities.Teacher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FilterWHUTeacherWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "whuteacher";
    private static AlumniDAO alumniDAO = new AlumniDAO();

    private static List<String> teacherUrls = alumniDAO.read("wteacher", "website");
    private static List<String> collegeNames = alumniDAO.read("wteacher","collegename");
    private static List<String> schoolNames = alumniDAO.read("wteacher","schoolname");
    private static List<String> teacherSetNames = alumniDAO.read("wteacher","teachersetname");
    private static List<String> teacherNames = alumniDAO.read("wteacher","teachername");

    private Site site = new MySite().site;

    private void whuTeacherPage(Page page)
    {
            Whitelist whitelist = new Whitelist();
            String text = Jsoup.clean(page.getHtml().toString(), whitelist);
            if(WHUTool.isWHU(text))
            {
                Request request = page.getRequest();
                Teacher teacher = new Teacher(request.getExtra("_name").toString(), request.getExtra("_sname").toString(),
                        request.getExtra("_tname").toString(), request.getExtra("_tname2").toString(), request.getUrl());
                String rank = RankTool.getRank(text);
                alumniDAO.add(teacher,rank,dataSetName);
            }
    }

    @Override
    public void process(Page page) {
        System.out.print(page.getRequest().getExtra("_level") + " ");
        System.out.print(page.getRequest().getExtra("_name") + " ");
        System.out.print(page.getRequest().getExtra("_sname") + " ");
        System.out.print(page.getRequest().getExtra("_tname") + " ");
        System.out.print(page.getRequest().getExtra("_tname2") + " ");
        System.out.println(page.getUrl());

        whuTeacherPage(page);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Request[] requests = new Request[10000];

        for(int i = 0; i < 10000; i++)
        {
            requests[i] = new Request(" ");
        }

        for(int i = 0; i < teacherUrls.size(); ++i) {

            if(collegeNames.get(i).equals("武汉大学")) continue;
            if(teacherNames.get(i).contains("交流") || teacherNames.get(i).contains("访问")) continue;
            String url = teacherUrls.get(i);
            if(url.endsWith(".cn")) url += "/";

            if(url.endsWith(".pdf")) continue;

            if(!url.startsWith("http")) url = "http://" + url;

            String cname = collegeNames.get(i);
            String sname = schoolNames.get(i);
            String tname = teacherSetNames.get(i);
            String tname2 = teacherNames.get(i);
            Request request = null;
            request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", cname).
                    putExtra("_tname", tname).putExtra("_sname", sname).putExtra("_tname2", tname2);
            requests[i] = request;
        }
        Spider spider = Spider.create(new FilterWHUTeacherWebsitePageProcessor())
                .addRequest(requests)
                .thread(3);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();
    }
}
