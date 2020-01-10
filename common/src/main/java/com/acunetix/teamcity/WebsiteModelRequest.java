package com.acunetix.teamcity;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class WebsiteModelRequest extends ApiRequestBase {
    private String responseContent = "";
    private int responseStatusCode = 0;
    private HttpResponse response;

    public WebsiteModelRequest(@NotNull Map<String, String> scanParameters) throws  NullPointerException, URISyntaxException,IOException {
        super(scanParameters);
        pluginWebSiteModelsUri = new URL(ApiURL, "api/1.0/scans/PluginWebSiteModels").toURI();
        requestPluginWebSiteModels();
    }

    public final URI pluginWebSiteModelsUri;

    public void requestPluginWebSiteModels() throws IOException {
        final HttpClient httpClient = getHttpClient();
        final HttpGet httpGet = new HttpGet(pluginWebSiteModelsUri);
        httpGet.setHeader("Accept", xml);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        response = httpClient.execute(httpGet);
        responseStatusCode=response.getStatusLine().getStatusCode();

        if (responseStatusCode == 200) {
            responseContent = AppCommon.ParseResponseToString(response);
        }
    }

    public String getResponseContent() {
        return responseContent;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }
}
