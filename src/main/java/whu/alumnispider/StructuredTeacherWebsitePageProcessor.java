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
import whu.alumnispider.utilities.StructuredTeacher;

import java.util.ArrayList;
import java.util.List;

public class StructuredTeacherWebsitePageProcessor implements PageProcessor {


    private static AlumniDAO alumniDAO = new AlumniDAO();

    private String parent = "https://daoshi.eol.cn";
    private String teacherXpath = "//div[@class='info']/div[@class='tr']/div";

    private Site site = new MySite().site;

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        if(page.getRequest().getExtra("home").toString().equals("1"))
        {
            getTeacherPage(page);
        }
        else
        {
            addTeacher(page);
        }
    }



    public void getTeacherPage(Page page) {
        Document document = Jsoup.parse(page.getHtml().toString());
        JXDocument jxDocument = new JXDocument(document);
        List<JXNode> jxNodes = new ArrayList<>();
        try {
            jxNodes = jxDocument.selN(teacherXpath);
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < jxNodes.size(); ++i){
            try {
                List<JXNode> teacherNodes = jxNodes.get(i).sel("/div");
                String name = teacherNodes.get(0).getElement().text();
                String cname = teacherNodes.get(1).getElement().text();
                String position = teacherNodes.get(2).getElement().text();
                String major = teacherNodes.get(3).getElement().text();
                String field = teacherNodes.get(4).getElement().text();
                String href = parent + teacherNodes.get(6).sel("/a/@href").get(0).toString();

                if(cname.equals("武汉大学")) continue;

                Request request = new Request(href).putExtra("home", "0").putExtra("name", name)
                        .putExtra("cname", cname).putExtra("position", position)
                        .putExtra("major", major).putExtra("field", field);
                page.addTargetRequest(request);
            } catch (XpathSyntaxErrorException e) {
                e.printStackTrace();
            }
        }
    }

    public void addTeacher(Page page)
    {
        String processingHtml = page.getHtml().toString();
        if(processingHtml.contains("武汉大学")||processingHtml.contains("武大")
                ||processingHtml.contains("武汉水利电力大学")||processingHtml.contains("武汉测绘科技大学")||processingHtml.contains("湖北医科大学")
                ||processingHtml.contains("武汉水利电力学院")||processingHtml.contains("葛洲坝水电工程学院")||processingHtml.contains("武汉测绘学院")
                ||processingHtml.contains("武汉测量值图学院")||processingHtml.contains("湖北医学院")||processingHtml.contains("湖北省医学院")
                ||processingHtml.contains("湖北省立医学院")||processingHtml.contains("武汉水利水电学院"))
        {
            Request request = page.getRequest();
            String name = request.getExtra("name").toString();
            String cname = request.getExtra("cname").toString();
            String major = request.getExtra("major").toString();
            String position = request.getExtra("position").toString();
            String field = request.getExtra("field").toString();
            String website = page.getUrl().toString();
            StructuredTeacher structuredTeacher = new StructuredTeacher(name, cname, major, position, field, website);
            alumniDAO.add(structuredTeacher);
        }
    }

    public static void main(String[] args) {

        Request[] requests = new Request[5144];
        for(int i = 1; i <= 5144; ++i)
        {
            String string = "https://daoshi.eol.cn/daoshi/search/ranklists?first=no&page=" + i;
            Request request = new Request(string).putExtra("home", "1");
            requests[i-1] = request;
        }
        Spider spider = Spider.create(new StructuredTeacherWebsitePageProcessor())
                .addRequest(requests)
                .thread(3);

        HttpClientDownloader downloader = new BetterDownloader();
        spider.setDownloader(downloader);
        spider.run();

    }
}

