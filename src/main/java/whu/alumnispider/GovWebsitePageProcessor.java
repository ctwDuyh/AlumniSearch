package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.scheduler.LevelLimitScheduler;
import whu.alumnispider.utilities.GovSubpage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  2019-11-11
    Extract URl in Government's website by using BFS.
 */
public class GovWebsitePageProcessor implements PageProcessor {
    private static String dataSetName = "GovWebsiteSet";
    private static List<String> urls = new AlumniDAO().read(dataSetName, "url");
    private static List<String> organizers = new AlumniDAO().read(dataSetName,"organizer");

    // index represents the elements' index in database.
    private static int index = 0;
    private static int sum = 0;


    private static String hrefRegex = "<a .*href=.+</a>";
    private static Pattern hrefPattern = Pattern.compile(hrefRegex);
    private static String homePageRegex = "gov[.]cn/?$";
    private static Pattern homePagePattern = Pattern.compile(homePageRegex);
    private static String whuRegex = "武汉大学";
    private static Pattern whuPattern = Pattern.compile(whuRegex);

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

    private boolean IsHomePage (String url) {
        Matcher matcher = homePagePattern.matcher(url);
        return matcher.find();
    }

    @Override
    public void process(Page page) {
        String processingUrl = page.getUrl().toString();
        Map<String, Object> extras = new HashMap<String, Object>();

        if (((Integer)page.getRequest().getExtra("_level") < 3)) {
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
                    if (page.getRequest().getExtra("parentWebsite") == null
                            || href.startsWith(page.getRequest().getExtra("parentWebsite").toString())) {
                        if (href.charAt(0) == '.' && processingUrl.endsWith("/")) {
                            href = processingUrl + href.substring(2, href.length() - 1);
                        } else if (href.charAt(0) == '.') {
                            href = processingUrl + href.substring(1, href.length() - 1);
                        }

                        Request request = new Request(href).setPriority(2)
                                .putExtra("organizer", organizers.get(index))
                                .putExtra("_level", ((Integer) page.getRequest().getExtra("_level") + 1))
                                .putExtra("parentWebsite", processingUrl);
                        //System.out.println(request.getUrl().toString() + ": " + request.getExtra("_level"));
                        page.addTargetRequest(request);
                        sum++;
                        //keywordExtract(page);
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
        long sysDate = System.currentTimeMillis();
        //while(index < urls.size()) {
            //Request request = new Request(urls.get(index)).putExtra("_level", 0);
            Request request = new Request("http://www.wuhan.gov.cn/").setPriority(1).putExtra("_level", 1).putExtra("parentWebsite", "http://www.wuhan.gov.cn/");
            // System.out.println(url);
            Spider.create(new GovWebsitePageProcessor())
                    .addRequest(request)
                    .scheduler(new LevelLimitScheduler(3))
                    .thread(1)
                    .run();
            index++;
            System.out.println(sum);
        //}
        long costTime = System.currentTimeMillis() - sysDate;
        System.out.println(System.currentTimeMillis() - sysDate);
    }
}
