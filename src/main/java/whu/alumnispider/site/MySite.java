package whu.alumnispider.site;

import us.codecraft.webmagic.Site;

public class MySite {
    public Site site = Site.me().setSleepTime(1000).setCycleRetryTimes(5).setTimeOut(10000).setCharset("UTF-8")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7");
}
