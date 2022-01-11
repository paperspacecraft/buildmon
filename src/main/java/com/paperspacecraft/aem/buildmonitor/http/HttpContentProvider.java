package com.paperspacecraft.aem.buildmonitor.http;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Builder(builderClassName = "Builder")
class HttpContentProvider implements ContentProvider {
    private static final String ERROR_COULD_NOT_COMPLETE = "Could not complete the HTTP request to '%s'";

    @Getter
    private final String endpoint;

    private final String login;

    private final String password;

    private final Log logger;

    @Override
    public String getContent() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));

        HttpClient client = HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(provider)
                .build();

        HttpGet httpGet = new HttpGet(endpoint);

        HttpResponse response;
        try {
            response = client.execute(httpGet);
        } catch (IOException e) {
            logger.debug(String.format(ERROR_COULD_NOT_COMPLETE, endpoint), e);
            return null;
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            return null;
        }
        return getResponseAsString(response);
    }

    private String getResponseAsString(HttpResponse response) {
        String result = StringUtils.EMPTY;
        try {
            result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            logger.error(String.format(HttpTester.ERROR_COULD_NOT_PARSE, endpoint), e);
        }
        return result;
    }
}
