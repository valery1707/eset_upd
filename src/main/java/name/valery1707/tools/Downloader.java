package name.valery1707.tools;

import name.valery1707.tools.configuration.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static name.valery1707.tools.Utils.propagate;

public class Downloader implements Closeable {
    private final Configuration configuration;
    private final HttpHost httpHost;
    private final HttpClient httpClient;

    public Downloader(Configuration configuration) {
        this.configuration = configuration;
        httpHost = new HttpHost(configuration.getRemoteHost(), -1, configuration.getRemoteProtocol());
        httpClient = prepareHttpClient();
    }

    private HttpClient prepareHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword());
        AuthScope authScope = new AuthScope(httpHost);
        client.getCredentialsProvider().setCredentials(authScope, credentials);
        int maxRetries = Math.max(configuration.getMaxRetries(), 20);
        return new AutoRetryHttpClient(client, new DefaultServiceUnavailableRetryStrategy(maxRetries, 1000));
    }

    @Override
    public void close() throws IOException {
        httpClient.getConnectionManager().shutdown();
    }

    public File download(String urlPart) {
        log.debug("Downloading {}", urlPart);
        File file = new File(configuration.getPathTmp(), FilenameUtils.getName(urlPart));
        try {
            HttpResponse response = httpClient.execute(httpHost, prepareHttpRequest(new HttpGet(urlPart)), prepareHttpContext());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    && entity != null) {
                FileOutputStream outputStream = new FileOutputStream(file);
                entity.writeTo(outputStream);//todo rewrite with logging progress to logger
                outputStream.close();
            } else {
                //todo Throw Exception
            }
        } catch (IOException e) {
            throw propagate(e);
        }
        return file;
    }

    public long size(String url) {
        try {
            HttpResponse response = httpClient.execute(httpHost, prepareHttpRequest(new HttpHead(url)), prepareHttpContext());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Header len = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
                return Long.parseLong(len.getValue());
            } else {
                return -1;
            }
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    public <T extends HttpRequestBase> T prepareHttpRequest(T request) {
        request.setHeader(HttpHeaders.USER_AGENT, "ESS Update (Windows; U; 32bit; VDB 12378; BPC 4.2.71.3; OS: 6.1.7601 SP 1.0 NT; CH 1.2; LNG 1049; x32c; UPD http://update.eset.com/eset_upd/v4; APP eav; BEO 1; CPU 14156; ASP 0.10; FW 0.0; PX 0; PUA 1)");
        return request;
    }

    private HttpContext prepareHttpContext() {
        BasicHttpContext context = new BasicHttpContext();
//        AuthCache authCache = new BasicAuthCache();
//        DigestScheme scheme = new DigestScheme();
//        authCache.put(httpHost, scheme);
//        context.setAttribute(ClientContext.AUTH_CACHE, authCache);
        return context;
    }

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
}
