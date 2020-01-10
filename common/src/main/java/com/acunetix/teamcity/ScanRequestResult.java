package com.acunetix.teamcity;

import jetbrains.buildServer.http.HttpUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.parser.ParseException;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScanRequestResult extends ApiRequestBase {
    public static final String IsError_Literal = "IsError";
    public static final String HTTPStatusCode_Literal = "HTTPStatusCode";
    public static final String Message_Literal = "Message";
    public static final String Data_Literal = "Data";
    public static final String ScanTaskID_Literal = "ScanTaskID";
    public static final String ScanReportEndpoint_Literal = "ScanReportEndpoint";
    public static final String BuildID_Literal = "BuildID";

    private final URL serverURL;
    private final URI scanDataControllerURI;
    private final boolean isInServer;
    private final String scanReportEndpoint;
    private final String teamCityUserID;
    private final String teamCityPassword;
    private final long buildID;
    private final int httpStatusCode;
    private String data;

    private String scanTaskID;
    private boolean isError;
    private String errorMessage;

    private ScanReport report = null;
    private Date previousRequestTime;

    //called from agent with scan request response
    public ScanRequestResult(HttpResponse response, String serverURL, long buildID, String teamCityUserID, String teamCityPassword,
                             URL apiUrl) throws MalformedURLException, URISyntaxException {
        super();
        isInServer = false;
        this.serverURL = new URL(serverURL);
        this.teamCityUserID = teamCityUserID;
        this.teamCityPassword = teamCityPassword;
        scanDataControllerURI = new URL(this.serverURL, ScanConstants.SCAN_DATA_CONTROLLER_RELATIVE_URL).toURI();

        httpStatusCode = response.getStatusLine().getStatusCode();
        isError = httpStatusCode != 201;

        if (!isError) {
            try {
                data = AppCommon.ParseResponseToString(response);
                isError = !(boolean) AppCommon.ParseJsonValue(data, "IsValid");
                if (!isError) {
                    scanTaskID = (String) AppCommon.ParseJsonValue(data, "ScanTaskId");
                } else {
                    errorMessage = (String) AppCommon.ParseJsonValue(data, "ErrorMessage");
                }
            } catch (ParseException ex) {
                isError = true;
                errorMessage = "Scan request result is not parsable::: " + ex.toString();
            } catch (IOException ex) {
                isError = true;
                errorMessage = "Scan request result is not readable::: " + ex.toString();
            }
        }

        URL apiUrlToUse;
        if (apiUrl != null) {
            apiUrlToUse = apiUrl;
        } else {
            apiUrlToUse = ApiURL;
        }

        String scanReportRelativeUrl = "api/1.0/scans/report/";
        URI scanReportEndpointUri = new URL(apiUrlToUse, scanReportRelativeUrl).toURI();

        Map<String, String> queryparams = new HashMap<>();
        queryparams.put("Type", "ExecutiveSummary");
        queryparams.put("Format", "Html");
        queryparams.put("Id", scanTaskID);

        scanReportEndpoint = scanReportEndpointUri.toString() + "?" + AppCommon.MapToQueryString(queryparams);

        this.buildID = buildID;
    }

    //Called from server with request params
    public ScanRequestResult(Map<String, String> parameters) throws MalformedURLException {
        super(parameters);
        isInServer = true;
        serverURL = null;
        teamCityUserID = null;
        teamCityPassword = null;
        scanDataControllerURI = null;

        scanTaskID = parameters.get(ScanTaskID_Literal);
        buildID = Long.parseLong(parameters.get(BuildID_Literal));
        data = parameters.get(Data_Literal);
        isError = Boolean.parseBoolean(parameters.get(IsError_Literal));
        httpStatusCode = Integer.parseInt(parameters.get(HTTPStatusCode_Literal));
        scanReportEndpoint = parameters.get(ScanReportEndpoint_Literal);
    }


    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return isError;
    }

    public long getBuildID() {
        return buildID;
    }

    public String getScanTaskID() {
        return scanTaskID;
    }

    public int SendToServer() throws IOException {
        if (isInServer) {
            //do nothing you are already in the server
            return 0;
        } else {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(teamCityUserID, teamCityPassword);
            //UserAgent:Jakarta Commons-HttpClient/3.1
            //Content-Type: application/x-www-form-urlencoded
            HttpClient httpClient = HttpUtil.createHttpClient(
                    60, scanDataControllerURI.toURL(), credentials, true);

            PostMethod postMethod = new PostMethod(scanDataControllerURI.toString());
            NameValuePair[] nameValuePairs = new NameValuePair[6];
            nameValuePairs[0] = new NameValuePair(ScanTaskID_Literal, scanTaskID);
            nameValuePairs[1] = new NameValuePair(BuildID_Literal, String.valueOf(buildID));
            nameValuePairs[2] = new NameValuePair(Data_Literal, data);
            nameValuePairs[3] = new NameValuePair(IsError_Literal, String.valueOf(isError));
            nameValuePairs[4] = new NameValuePair(HTTPStatusCode_Literal, String.valueOf(httpStatusCode));
            nameValuePairs[5] = new NameValuePair(ScanReportEndpoint_Literal, String.valueOf(scanReportEndpoint));

            postMethod.setRequestBody(nameValuePairs);

            return httpClient.executeMethod(postMethod);
        }
    }

    private boolean canAskForReportFromNCCloud() {
        Date now = new Date();
        //Is report not requested or have request threshold passed
        //And report isn't generated yet
        boolean isTimeThresholdPassed = previousRequestTime == null || now.getTime() - previousRequestTime.getTime() >= 60 * 1000;//1 min
        return !isReportAvailable() && isTimeThresholdPassed;
    }


    private boolean isReportAvailable() {
        return report != null && report.isReportGenerated();
    }

    public ScanReport getReport(String apiToken) {
        // if report is not generated and requested yet, request it from Acunetix 360 server.
        if (canAskForReportFromNCCloud()) {
            final ScanReport reportFromNcCloud = getReportFromNcCloud(apiToken);
            previousRequestTime = new Date();

            return reportFromNcCloud;
        }

        return report;
    }

    private ScanReport getReportFromNcCloud(String apiToken) {
        ScanReport report;

        if (!isError) {
            try {
                final org.apache.http.client.HttpClient httpClient = getHttpClient();
                final HttpGet httpGet = new HttpGet(scanReportEndpoint);
                httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader(apiToken));

                HttpResponse response = httpClient.execute(httpGet);
                report = new ScanReport(response, scanReportEndpoint);
            } catch (IOException ex) {
                String reportRequestErrorMessage = "Report result is not readable::: " + ex.toString();
                report = new ScanReport(false, "",
                        true, reportRequestErrorMessage, scanReportEndpoint);
            }
        } else {
            report = new ScanReport(true, errorMessage,
                    false, "", scanReportEndpoint);
        }

        this.report = report;

        return report;
    }
}
