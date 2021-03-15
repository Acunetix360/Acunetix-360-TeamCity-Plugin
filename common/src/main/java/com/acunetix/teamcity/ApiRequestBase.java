package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;

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
    public static final String PROXY_Used = "acunetixProxyUsed";
    public static final String PROXY_Host = "acunetixProxyHost";
    public static final String PROXY_Port = "acunetixProxyPort";
    public static final String PROXY_Username = "acunetixProxyUsername";
    public static final String PROXY_Password = "acunetixProxyPassword";
    public static final String PROXY_Password_ENCRYPTED = "acunetixEncryptedProxyPassword";

    public final Map<String, String> Parameters;
    public final URL ApiURL;
    public final String ApiToken;
    public final String ApiEncryptedToken;
    public final Boolean ProxyUsed;
    public final String ProxyHost;
    public final String ProxyPort;
    public final String ProxyUsername;
    public final String ProxyPassword;
    public final String ProxyPasswordEncrypted;

    public ApiRequestBase(Map<String, String> parameters) throws MalformedURLException {
        Parameters = parameters;
        if (parameters.get(API_URL_Literal) != null) {
            ApiURL = AppCommon.GetBaseURL(parameters.get(API_URL_Literal));
            ApiToken = parameters.get(API_TOKEN_Literal);
            ApiEncryptedToken = parameters.get(API_ENCRYPTED_TOKEN_Literal);
            ProxyUsed = Boolean.getBoolean(parameters.get(PROXY_Used));
            ProxyHost = parameters.get(PROXY_Host);
            ProxyPort = parameters.get(PROXY_Port);
            ProxyUsername = parameters.get(PROXY_Username);
            ProxyPassword = parameters.get(PROXY_Password);
            ProxyPasswordEncrypted = parameters.get(PROXY_Password_ENCRYPTED);
        } else {
            ApiURL = null;
            ApiToken = null;
            ApiEncryptedToken = null;
            ProxyUsed = null;
            ProxyHost = null;
            ProxyPort = null;
            ProxyUsername = null;
            ProxyPassword = null;
            ProxyPasswordEncrypted = null;
        }
    }

    public ApiRequestBase() {
        Parameters = null;
        ApiURL = null;
        ApiToken = null;
        ApiEncryptedToken = null;
        ProxyUsed = null;
        ProxyHost = null;
        ProxyPort = null;
        ProxyUsername = null;
        ProxyPassword = null;
        ProxyPasswordEncrypted = null;
    }

    protected HttpClient getHttpClient() {

        boolean useProxy = Boolean.parseBoolean(this.Parameters.get(PROXY_Used));

        if(!useProxy){
            return HttpClientBuilder
                    .create()
                    .build();
        }
        else{

            String authPassword ="";

            final String authUser = this.Parameters.get(PROXY_Username);
            authPassword = RSACipher.decryptWebRequestData(this.Parameters.get(PROXY_Password_ENCRYPTED));

            //This block was written because the value "PROXY_Password_ENCRYPTED" from ScanBuildParametersPreprocessor was unable to be decrypted.
            if(StringUtil.isEmptyOrSpaces(authPassword) || authPassword == null){
                authPassword = this.Parameters.get(PROXY_Password);
            }

            final String host = this.Parameters.get(PROXY_Host);
            final Integer port = Integer.parseInt(this.Parameters.get(PROXY_Port));

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(host, port),
                    new UsernamePasswordCredentials(authUser, authPassword));

            HttpHost myProxy = new HttpHost(host, port);

            HttpClientBuilder  clientBuilder = HttpClientBuilder.create();

            return clientBuilder
                    .setProxy(myProxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(credsProvider)
                    .disableCookieManagement().build();
        }
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
