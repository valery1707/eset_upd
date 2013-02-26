package name.valery1707.tools;

import name.valery1707.tools.configuration.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        int maxRetries = Math.min(configuration.getMaxRetries(), 20);
        return new AutoRetryHttpClient(client, new DefaultServiceUnavailableRetryStrategy(maxRetries, 1000));
    }

    @Override
    public void close() throws IOException {
        httpClient.getConnectionManager().shutdown();
    }

    private static final int[] GET_SCs = new int[]{HttpStatus.SC_OK};

    public File download(String urlPart) throws IOException {
        log.debug("Downloading {}", urlPart);
        File file = new File(configuration.getPathTmp(), FilenameUtils.getName(urlPart));

        HttpResponse response = execute(new HttpGet(urlPart), GET_SCs);
        HttpEntity entity = response.getEntity();

        FileOutputStream outputStream = new FileOutputStream(file);
        entity.writeTo(outputStream);//todo rewrite with logging progress to logger
        outputStream.close();
        //todo Show speed

        return file;
    }

    private static final int[] HEAD_SCs = new int[]{HttpStatus.SC_OK, HttpStatus.SC_UNAUTHORIZED};

    public long size(String url) throws IOException {
        HttpResponse response = execute(new HttpHead(url), HEAD_SCs);
        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                Header len = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
                return Long.parseLong(len.getValue());
            case HttpStatus.SC_UNAUTHORIZED:
            default:
                return -1;
        }
    }

    private HttpResponse execute(HttpRequestBase request, int[] successStatusCodes) throws IOException {
        Throwable lastThrowable = null;
        StatusLine lastStatusLine = null;
        for (int attempt = 0; attempt < configuration.getMaxRetries(); attempt++) {
            try {
                HttpResponse response = httpClient.execute(httpHost, prepareHttpRequest(request));
                lastStatusLine = response.getStatusLine();
                int statusCode = lastStatusLine.getStatusCode();
                if (ArrayUtils.contains(successStatusCodes, statusCode)) {
                    return response;
                }
            } catch (IOException e) {
                lastThrowable = e;
            }
        }
        String errorMessage = "Connection problem" + (lastStatusLine != null ? ": " + lastStatusLine.toString() : "");
        if (lastThrowable == null) {
            throw new IOException(errorMessage);
        } else {
            throw new IOException(errorMessage, lastThrowable);
        }
    }

    public <T extends HttpRequestBase> T prepareHttpRequest(T request) {
        //todo move to configuration, and replace some parts with real values
        request.setHeader(HttpHeaders.USER_AGENT, "ESS Update (Windows; U; 32bit; VDB 12378; BPC 4.2.71.3; OS: 6.1.7601 SP 1.0 NT; CH 1.2; LNG 1049; x32c; UPD http://update.eset.com/eset_upd/v4; APP eav; BEO 1; CPU 14156; ASP 0.10; FW 0.0; PX 0; PUA 1)");
        return request;
    }

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
}
