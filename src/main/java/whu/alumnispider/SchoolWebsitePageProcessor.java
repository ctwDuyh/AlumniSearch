package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.scheduler.LevelLimitScheduler;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovSubpage;
import whu.alumnispider.utilities.School;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  2019-11-11
    Extract URl in Government's website by using BFS.
 */
public class SchoolWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "school";
    private static List<String> collegeUrls = new AlumniDAO().read("college", "website");
    private static List<String> collegeNames = new AlumniDAO().read("college","collegename");

    private static AlumniDAO alumniDAO = new AlumniDAO();

    // index represents the elements' index in database.
    private static int index = 0;
    private static int maxLevel = 3;


    private static String hrefRegex = "<a .*href=.+</a>";
    private static Pattern hrefPattern = Pattern.compile(hrefRegex);
    private static String schoolRegex = "edu.cn/?$";
    private static Pattern schoolPattern = Pattern.compile(schoolRegex);
    private static String whuRegex = "武汉大学";
    private static Pattern whuPattern = Pattern.compile(whuRegex);

    static Set<School> extras = new HashSet<School>();

    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    /*  extract the keyword in website.
        And add these website into database.
        19-11-14*/
    private void keywordExtract(Page page) {
        String processingText = page.getHtml().toString();
        Matcher whuMatcher = whuPattern.matcher(processingText);

        if (whuMatcher.find()) {
            GovSubpage govSubpage = new GovSubpage();
            govSubpage.setOrganizer(page.getRequest().getExtra("Organizer").toString());
            govSubpage.setUrl(page.getUrl().toString());

            alumniDAO.add(govSubpage, "extractedPages");
        }
    }

    @Override
    public void process(Page page) {
        String processingUrl = page.getUrl().toString();
        System.out.println(page.getRequest().getExtra("_level") + ": " + processingUrl);

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

            // Delete the extracted empty href.
            if (!href.equals("")&&!href.equals("#")&&!href.equals("")) {
                if (href.charAt(0) == '.' && processingUrl.endsWith("/")) {
                    href = processingUrl + href.substring(2, href.length() - 1);
                } else if (href.charAt(0) == '.') {
                    href = processingUrl + href.substring(1, href.length() - 1);
                }

                if(!href.contains("http") && !href.contains("www")) href = page.getRequest().getExtra("parent") + href;

                School school = new School(page.getRequest().getExtra("_name").toString(), href);
                if(!extras.contains(school))
                {
                    extras.add(school);

                    Matcher schoolMatcher = schoolPattern.matcher(school.getWebsite());
                    if(schoolMatcher.find())
                    {
                        alumniDAO.add(school,dataSetName);
                    }

                    if (((Integer)page.getRequest().getExtra("_level") < maxLevel) && school.getWebsite().startsWith(page.getRequest().getExtra("parent").toString())) {
                        Request request = new Request(school.getWebsite()).setPriority(9-(Integer)page.getRequest().getExtra("_level"))
                                .putExtra("_name", school.getName())
                                .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1))
                                .putExtra("parent", page.getRequest().getExtra("parent"));
                        page.addTargetRequest(request);
                    }
                }
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Request[] requests = new Request[2000];
        for(int i = 0; i < 2000; i++)
        {
            requests[i] = new Request(" ");
        }
        while(index < collegeUrls.size()) {
            System.out.println(index);
            String url = collegeUrls.get(index);
            if(url.charAt(url.length()-1)!='/') url += "/";

            Matcher schoolMatcher = schoolPattern.matcher(url);
            if(!schoolMatcher.find()) {
                index++;
                continue;
            }

            String name = collegeNames.get(index);
            School school = new School(name, url);
            alumniDAO.add(school, dataSetName);
            extras.add(school);
            Request request = new Request(url).setPriority(10).putExtra("_level", 0).putExtra("_name", name);
            requests[index] = request;
            index++;
        }
        Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(requests)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(1)
                .run();
        /*
        School school = new School("武汉大学", "https://www.whu.edu.cn/");
        new AlumniDAO().add(school, dataSetName);
        extras.add(school);
        Request request = new Request(school.getWebsite()).setPriority(10).putExtra("_level", 0).putExtra("_name", school.getName()).putExtra("parent", school.getWebsite());
        Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(request)
                //.scheduler(new LevelLimitScheduler(3))
                .thread(3)
                .run();
         */
    }
}
