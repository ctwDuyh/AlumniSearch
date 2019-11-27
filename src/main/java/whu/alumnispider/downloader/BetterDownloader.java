package whu.alumnispider.downloader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;
import us.codecraft.webmagic.utils.HttpClientUtils;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.IOException;

public class BetterDownloader extends HttpClientDownloader {
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
            if (!request.isBinaryContent()) {
                if(charset==null) charset = CharsetUtils.detectCharset(contentType, bytes);
                page.setCharset(charset);//这里替换成要爬的网页的编码方式
                page.setRawText(new String(bytes, charset));
            }
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            page.setDownloadSuccess(true);
            page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
        }
        return page;
    }
}
