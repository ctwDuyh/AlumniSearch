package whu.alumnispider.tool;

public class UrlTool {
    public static String getPreparedUrl(String url)
    {
        if(url.endsWith(".cn")) url += "/";

        if(url.startsWith("https")) url = "http" + url.substring(5);

        if(!url.startsWith("http")) url = "http://" + url;

        return url;
    }
}
