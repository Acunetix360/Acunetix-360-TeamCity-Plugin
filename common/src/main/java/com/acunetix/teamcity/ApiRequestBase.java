package com.acunetix.teamcity;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class ApiRequestBase {
    protected static final String json = "application/json";
    protected static final String xml = "application/xml";

    //API settings property names start with "acunetix" to be unique in Teamcity environment.
    public static final String API_URL_Literal = "acunetixServerURL";
    public static final String API_TOKEN_Literal = "acunetixApiToken";
    public static final String API_ENCRYPTED_TOKEN_Literal = "acunetixEncryptedApiToken";

    public final Map<String, String> Parameters;
    public final URL ApiURL;
    public final String ApiToken;
    public final String ApiEncryptedToken;

    public ApiRequestBase(Map<String, String> parameters) throws MalformedURLException {
        Parameters = parameters;
        if (parameters.get(API_URL_Literal) != null) {
            ApiURL = AppCommon.GetBaseURL(parameters.get(API_URL_Literal));
            ApiToken = parameters.get(API_TOKEN_Literal);
            ApiEncryptedToken = parameters.get(API_ENCRYPTED_TOKEN_Literal);
        } else {
            ApiURL = null;
            ApiToken = null;
            ApiEncryptedToken = null;
        }
    }

    public ApiRequestBase() {
        Parameters = null;
        ApiURL = null;
        ApiToken = null;
        ApiEncryptedToken = null;
    }

    protected HttpClient getHttpClient() {
        return HttpClientBuilder
                .create()
                .build();
    }

    protected String getAuthHeader() {
        return getAuthHeader(null);
    }

    protected String getAuthHeader(String apiToken) {
        if (apiToken == null || apiToken.isEmpty()) {
            apiToken = ApiToken;
        }

        String auth = ":" + apiToken;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }
}
