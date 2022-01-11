package com.paperspacecraft.aem.buildmonitor.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpTester {

    static final String ERROR_COULD_NOT_PARSE = "Could not parse the HTTP response from '%s'";

    private ContentProvider contentProvider;
    private String mustContainHtml;
    private String mustContainText;
    private Log logger;


    public boolean test() {
        String content = contentProvider.getContent();
        if (content == null) {
            return false;
        }
        if (StringUtils.isAllEmpty(mustContainHtml, mustContainText)) {
            return true;
        }

        if (StringUtils.isNotEmpty(mustContainHtml)) {
            return hasHtmlElement(content, mustContainHtml);
        }
        return StringUtils.contains(content, mustContainText);
    }

    public String getEndpoint() {
        return Optional.ofNullable(contentProvider).map(ContentProvider::getEndpoint).orElse(StringUtils.EMPTY);
    }

    private boolean hasHtmlElement(String content, String query) {
        String selector = query;
        String value = null;
        if (query.contains("|")) {
            selector = StringUtils.substringBefore(query, "|").trim();
            value = StringUtils.substringAfter(query, "|").trim();
        }
        try {
            Document document = Jsoup.parse(content);
            Elements result = document.select(selector);
            if (StringUtils.isEmpty(value)) {
                return !result.isEmpty();
            }
            return StringUtils.contains(result.get(0).text(), value);
        } catch (Exception e) {
            logger.error(String.format(ERROR_COULD_NOT_PARSE, contentProvider.getEndpoint()), e);
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private ContentProvider contentProvider;
        private String endpoint;
        private String userName;
        private String password;
        private String mustContainHtml;
        private String mustContainText;
        private Log logger;

        public Builder contentProvider(ContentProvider value) {
            this.contentProvider = value;
            return this;
        }

        public Builder endpoint(String value) {
            this.endpoint = value;
            return this;
        }

        public Builder userName(String value) {
            this.userName = value;
            return this;
        }

        public Builder password(String value) {
            this.password = value;
            return this;
        }

        public Builder mustContainHtml(String value) {
            this.mustContainHtml = value;
            return this;
        }

        public Builder mustContainText(String value) {
            this.mustContainText = value;
            return this;
        }

        public Builder logger(Log value) {
            this.logger = value;
            return this;
        }

        public HttpTester build() {
            HttpTester result = new HttpTester();
            result.contentProvider = contentProvider != null
                    ? contentProvider
                    : HttpContentProvider.builder().endpoint(endpoint).login(userName).password(password).logger(logger).build();
            result.mustContainHtml = mustContainHtml;
            result.mustContainText = mustContainText;
            result.logger = logger;
            return result;
        }
    }
}
