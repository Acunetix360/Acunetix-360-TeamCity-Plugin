package com.acunetix.teamcity;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;


public class ScanBuildProcessAdapter extends AbstractBuildProcessAdapter {
    public ScanBuildProcessAdapter(
            @NotNull final ArtifactsWatcher artifactsWatcher,
            @NotNull final AgentRunningBuild build,
            @NotNull final BuildRunnerContext context,
            @NotNull final BuildProgressLogger progressLogger) throws RunBuildException {
        super(artifactsWatcher, build, context, progressLogger);
    }

    @Override
    protected void runProcess() throws RunBuildException {
        try {
            if (!isInterrupted()) {
                ScanRequestHandler(runnerParameters);
            } else {
                throw new RunBuildException("Scan request is interrupted.");
            }
        } catch (Exception e) {
            throw new RunBuildException(e);
        }
    }

    private void ScanRequestHandler(Map<String, String> runnerParameters) throws Exception {
        progressLogger.message("Getting build parameters...");
        ScanRequest scanRequest = new ScanRequest(runnerParameters);
        Map<String, String> systemProperties = context.getBuildParameters().getSystemProperties();
        String teamCityUserID = systemProperties.get("teamcity.auth.userId");
        String teamCityPassword = systemProperties.get("teamcity.auth.password");

        progressLogger.message("Requesting scan...");
        HttpResponse scanRequestResponse = scanRequest.scanRequest();
        progressLogger.message("Scan request status code: " + scanRequestResponse.getStatusLine().getStatusCode());
        ScanRequestResult scanRequestResult =
                new ScanRequestResult(scanRequestResponse, getServerURL(), build.getBuildId(), teamCityUserID,
                        teamCityPassword, scanRequest.ApiURL);

        // HTTP status code 201 refers to created. This means our request added to queue. Otherwise it is failed.
        if (scanRequestResult.getHttpStatusCode() == 201 && !scanRequestResult.isError()) {
            ScanRequestSuccessHandler(scanRequestResult);
        } else {
            ScanRequestFailureHandler(scanRequestResult);
        }
    }

    private void ScanRequestSuccessHandler(ScanRequestResult scanRequestResult) throws IOException {
        progressLogger.message("Scan requested successfully.");
        progressLogger.message("Sending scan request result to server...");
        int code = scanRequestResult.SendToServer();
        LogScanDataControllerResponse(code);
    }

    private void ScanRequestFailureHandler(ScanRequestResult scanRequestResult) throws IOException, RunBuildException {
        progressLogger.error("Scan request failed. Error Message: " + scanRequestResult.getErrorMessage());
        progressLogger.message("Sending scan request result to server...");
        int code = scanRequestResult.SendToServer();
        LogScanDataControllerResponse(code);

        throw new RunBuildException("Failed to start the scan. Response status code: " + scanRequestResult.getHttpStatusCode());
    }

    private void LogScanDataControllerResponse(int responseStatusCode) {
        //200 means scan request result sent to server successfully.
        if (responseStatusCode == 200) {
            progressLogger.message("Scan request result successfully sent to server.");
        } else {
            progressLogger.error("Failed to send scan request result to server. Response status code: " + responseStatusCode);
        }
    }

}
