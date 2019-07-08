package com.acunetix.teamcity;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScanRequest extends ApiRequestBase {
    //API settings property names start with "acunetix" to be unique in Teamcity environment.
    public static final String SCAN_TYPE_Literal = "acunetixScanType";
    public static final String WEBSITE_ID_Literal = "acunetixWebsiteID";
    public static final String PROFILE_ID_Literal = "acunetixProfileID";
    public final URI scanUri;
    public final URI testUri;
    public final ScanType scanType;
    public final String websiteId;
    public final String profileId;
    public final VCSCommit vcsCommit;

    public ScanRequest(@NotNull Map<String, String> scanParameters) throws MalformedURLException, NullPointerException, URISyntaxException {
        super(scanParameters);
        scanUri = new URL(ApiURL, "api/v1/integrationsApi/CreateFromPluginScanRequest").toURI();
        testUri = new URL(ApiURL, "api/v1/integrationsApi/VerifyPluginScanRequest").toURI();
        scanType = ScanType.valueOf(scanParameters.get(SCAN_TYPE_Literal));
        websiteId = scanParameters.get(WEBSITE_ID_Literal);
        profileId = scanParameters.get(PROFILE_ID_Literal);
        vcsCommit = new VCSCommit(scanParameters);
    }

    public HttpResponse scanRequest() throws IOException {
        HttpClient client = getHttpClient();

        final HttpPost httpPost = new HttpPost(scanUri);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        List<NameValuePair> params = new ArrayList<>();
        setScanParams(params);
        vcsCommit.addVcsCommitInfo(params);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        return client.execute(httpPost);
    }

    private void setScanParams(List<NameValuePair> params) {
        switch (scanType) {
            case FullWithPrimaryProfile:
                params.add(new BasicNameValuePair("WebsiteId", websiteId));
                params.add(new BasicNameValuePair("ScanType", "FullWithPrimaryProfile"));
                break;
            case FullWithSelectedProfile:
                params.add(new BasicNameValuePair("WebsiteId", websiteId));
                params.add(new BasicNameValuePair("ProfileId", profileId));
                params.add(new BasicNameValuePair("ScanType", "FullWithSelectedProfile"));
                break;
        }
    }
}
