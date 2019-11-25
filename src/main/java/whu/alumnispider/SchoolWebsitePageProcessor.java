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

            new AlumniDAO().add(govSubpage, "extractedPages");
        }
    }

    @Override
    public void process(Page page) {
        String processingUrl = page.getUrl().toString();

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
            if (!href.equals("")) {
                if (href.charAt(0) == '.' && processingUrl.endsWith("/")) {
                    href = processingUrl + href.substring(2, href.length() - 1);
                } else if (href.charAt(0) == '.') {
                    href = processingUrl + href.substring(1, href.length() - 1);
                }

                School school = new School(page.getRequest().getExtra("_name").toString(), href);
                if(!extras.contains(school))
                {
                    extras.add(school);

                    Matcher schoolMatcher = schoolPattern.matcher(school.getWebsite());
                    if(schoolMatcher.find())
                    {
                        new AlumniDAO().add(school,dataSetName);
                    }

                    if (((Integer)page.getRequest().getExtra("_level") < maxLevel)) {
                        Request request = new Request(school.getWebsite()).setPriority(2)
                                .putExtra("_name", school.getName())
                                .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1));
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
        /*while(index < collegeUrls.size()) {
            String url = collegeUrls.get(index);
            String name = collegeNames.get(index);
            School school = new School(name, url);
            new AlumniDAO().add(school, dataSetName);
            extras.add(school);
            Request request = new Request(url).setPriority(1).putExtra("_level", 0).putExtra("_name", name);
            Spider.create(new SchoolWebsitePageProcessor())
                    .addRequest(request)
                    .scheduler(new LevelLimitScheduler(3))
                    .thread(1)
                    .run();
            index++;
        }*/
        School school = new School("武汉大学", "https://www.whu.edu.cn/");
        new AlumniDAO().add(school, dataSetName);
        extras.add(school);
        Request request = new Request(school.getWebsite()).setPriority(1).putExtra("_level", 0).putExtra("_name", school.getName());
        Spider.create(new SchoolWebsitePageProcessor())
                .addRequest(request)
                .scheduler(new LevelLimitScheduler(3))
                .thread(1)
                .run();
    }
}
