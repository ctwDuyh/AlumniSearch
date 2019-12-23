package whu.alumnispider.tool;

public class HrefTool {
    public static String getHref(String href, String parent, String processUrl)
    {
        href.trim();
        if(href.startsWith("http")|| href.startsWith("www")) return UrlTool.getPreparedUrl(href);
        if(href.startsWith("www"))
        if(href.charAt(0) == '/')
        {
            return parent + href.substring(1);
        }
        else
        {
            if(!processUrl.endsWith("/"))
            {
                processUrl = processUrl.substring(0,processUrl.lastIndexOf("/")+1);
            }

            while(href.charAt(0)=='.')
            {
                href = href.substring(1);
                processUrl = processUrl.substring(0, processUrl.lastIndexOf("/"));
            }

            if(processUrl.endsWith("/"))
            {
                return processUrl + href;
            }
            else
            {
                return getHref(href.substring(1), parent, processUrl+"/");
            }
        }
    }
}
