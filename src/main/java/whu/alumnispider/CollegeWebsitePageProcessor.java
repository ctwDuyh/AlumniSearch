package whu.alumnispider;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpClientUtils;
import us.codecraft.webmagic.utils.HttpConstant;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// 2019-11-4
public class CollegeWebsitePageProcessor implements PageProcessor {
    private static String homeLinkRe = "http://college.gaokao.com/schlist/p\\d+/";
    private static String collegeLinkRe = "http://college.gaokao.com/school/\\d+/";
    private static Pattern homeLinkPattern = Pattern.compile(homeLinkRe);
    private static Pattern collegeLinkPattern = Pattern.compile(collegeLinkRe);

    private static AlumniDAO alumniDAO = new AlumniDAO();

    private Site site = Site.me().setSleepTime(300).setCycleRetryTimes(5).setTimeOut(10000).setCharset("UTF-8")
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "college";

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable collegePage;

        List<String> collegePages = new ArrayList<String>();
        // Below code is the definition of Xpath sentence.
        String collegeLinkXpath = "//div[@class='scores_List']/dl/dt/a/@href";
        String processingUrl = page.getUrl().toString();
        Matcher homeLinkMatcher = homeLinkPattern.matcher(processingUrl);
        Matcher collegeLinkMatcher = collegeLinkPattern.matcher(processingUrl);

        if (homeLinkMatcher.find()) {
            // To get colleges' pages
            collegePage = page.getHtml().xpath(collegeLinkXpath);
            collegePages = collegePage.all();
            page.addTargetRequests(collegePages, 1);
        }
        else if(collegeLinkMatcher.find())
        {
            getCollegePage(page);
        }
        System.out.println();
    }

    public void getCollegePage(Page page) {
        String collegeNameXpath = "//div[@class='bg_sez']/h2/text()";
        String collegeContentXpath = "//div[@class='college_msg bk']/dl/dd/ul[@class='left contact']";
        Selectable collegeName;
        Selectable collegeContent;
        College college = new College();

        collegeName = page.getHtml().xpath(collegeNameXpath);
        collegeContent = page.getHtml().xpath(collegeContentXpath);

        college.setName(collegeName.toString());
        String content = collegeContent.toString();
        int start = content.indexOf("学校网址：");
        int end = content.lastIndexOf(".cn");
        if(start==-1||end==-1) return;
        String website = content.substring(start+5,end+3);
        String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则
        website = website.replaceAll(REGEX_CHINESE, "");
        college.setWebsite(website);

        alumniDAO.add(college, tableName);
    }

    public static void main(String[] args) {

        String strings[] = new String[120];
        for(int i = 0; i < 120; ++i)
        {
            strings[i] = "http://college.gaokao.com/schlist/p" + (i+1) + "/";
        }
        Spider spider = Spider.create(new CollegeWebsitePageProcessor())
                .addUrl(strings)
                .thread(1);
        HttpClientDownloader downloader = new HttpClientDownloader(){
            @Override
            protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
                Page page = new Page();
                if (httpResponse.getStatusLine().getStatusCode() != HttpConstant.StatusCode.CODE_200) {
                    page.setDownloadSuccess(false);
                    System.err.println("error: " + httpResponse.getStatusLine().getStatusCode() + " " + request.getUrl());
                } else {
                    byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                    String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
                    page.setBytes(bytes);
                    if (!request.isBinaryContent()){
                        page.setCharset("gb2312");//这里替换成要爬的网页的编码方式
                        page.setRawText(new String(bytes, "gb2312"));
                    }
                    page.setUrl(new PlainText(request.getUrl()));
                    page.setRequest(request);
                    page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
                    page.setDownloadSuccess(true);
                    page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
                }
                return page;
            }

        };

        spider.setDownloader(downloader);
        spider.run();


/*
        String strings[] = new String[120];
        for(int i = 0; i < 120; ++i)
        {
            strings[i] = "http://college.gaokao.com/school/" + (i+1) + "/";
        }
        Spider spider = Spider.create(new CollegeWebsitePageProcessor())
                .addUrl(strings)
                .thread(1);
        HttpClientDownloader downloader = new HttpClientDownloader(){
            @Override
            protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
                Page page = new Page();
                if (httpResponse.getStatusLine().getStatusCode() != HttpConstant.StatusCode.CODE_200) {
                    page.setDownloadSuccess(false);
                } else {
                    byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                    String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
                    page.setBytes(bytes);
                    if (!request.isBinaryContent()){
                        page.setCharset("gb2312");
                        page.setRawText(new String(bytes, "gb2312"));
                    }
                    page.setUrl(new PlainText(request.getUrl()));
                    page.setRequest(request);
                    page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
                    page.setDownloadSuccess(true);
                    page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
                }
                return page;
            }

        };

        spider.setDownloader(downloader);
        spider.run();
*/
    }
}

