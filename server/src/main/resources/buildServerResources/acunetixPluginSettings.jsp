<%@ page import="com.acunetix.teamcity.ApiRequestBase" %>
<%@include file="/include.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="pluginSettingsManager" type="com.acunetix.teamcity.PluginSettingsManager" scope="request"/>
<c:url var="controllerUrl" value="/acunetix/pluginsettings.html"/>
<c:url var="logoUrl" value="${teamcityPluginResourcesPath}images/logo.svg"/>

<script type="text/javascript">
    var pluginSettingsForm = OO.extend(BS.AbstractPasswordForm, {
        formElement: function () {
            return $("acunetixPluginSettingsForm")
        },
        save: function () {
            BS.PasswordFormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
                onAcunetixApiURLError: function (elem) {
                    $("error_acunetixApiURL").innerHTML = elem.firstChild.nodeValue;
                    pluginSettingsForm.highlightErrorField($("acunetixServerURL"));
                },

                onAcunetixApiTokenError: function (elem) {
                    $("error_acunetixApiToken").innerHTML = elem.firstChild.nodeValue;
                    pluginSettingsForm.highlightErrorField($("acunetixApiToken"));
                },

                onSuccessfulSave: function () {
                    pluginSettingsForm.enable();
                },

                onCompleteSave: function (form, responseXml, wereErrors) {
                    BS.ErrorsAwareListener.onCompleteSave(form, responseXml, wereErrors);
                    if (!wereErrors) {
                        $('pluginSettingsContainer').refresh();
                    }
                }
            }));

            return false;
        }
    });
</script>
<div>
    <bs:refreshable containerId="pluginSettingsContainer" pageUrl="${pageUrl}">
        <bs:messages key="settingsSaved"/>
        <form id="acunetixPluginSettingsForm" action="${controllerUrl}"
              method="post" onsubmit="{return pluginSettingsForm.save()}">
            <table class="runnerFormTable">
                <tr>
                    <td colspan="2">
                    <div style="color: #3f3f3f;display:inline;font-size: 130%;">
                        <img src="${logoUrl}" alt="Acunetix 360"
                             style="vertical-align:top; margin-bottom:1px;display: inline-block;height:1.1em;width: auto;"/>
                        <span style="display:inline-block;zoom:1;color: #3f3f3f;font-size: 130%;">
                            Acunetix
                        </span>
                    </div>
                    </td>
                </tr>
                <l:settingsGroup title="API Settings">
                <tr>
                    <th>
                        <label for="acunetixServerURL">Acunetix 360 Server URL:<l:star/>
                            <bs:helpIcon iconTitle="like 'https://www.acunetix360.com'"/>
                        </label>
                    </th>
                    <td>
                        <input type="text" name="acunetixServerURL" id="acunetixServerURL"
                               value="${pluginSettingsManager.pluginSettings.serverURL}" class="longField">
                        <span class="error" id="error_acunetixApiURL"></span>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="acunetixApiToken">API Token:<l:star/>
                            <bs:helpIcon iconTitle="It can be found at 'Your Account > API Settings' page in the Acunetix 360.<br/>
                         User must have 'Start Scans' permission for the target website."/>
                        </label>
                    </th>
                    <td>
                        <input type="password" name="acunetixApiToken" id="acunetixApiToken"
                               value="${pluginSettingsManager.random}"
                               class="longField textProperty"/>

                        <input type="hidden" id="acunetixEncryptedApiToken"
                               name="acunetixEncryptedApiToken"
                               value="${pluginSettingsManager.pluginSettings.encryptedApiToken}"/>

                        <input type="hidden" id="acunetixApiTokenInitial"
                               name="acunetixApiTokenInitialValue" value=""/>

                        <span class="error" id="error_acunetixApiToken"></span>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <a class="btn btn_hint" id="acunetixTestConnectionButton">Test Connection</a>
                        <span id="acunetixConnectionResult"></span>
                    </td>
                </tr>
                </l:settingsGroup>
            </table>
            <script>
                var ncServerURLInput, ncApiTokenInput, ncApiTokenInitialValueInput, ncEncryptedApiTokenInput,
                    ncPublicKeyInput;
                var ncTestConnectionResultSpan, ncTestConnectionButton;
                var ncScanParams = {}, ncTestRequestParams = {};
                //do noy use $ for Jquery instead use jQuery
                jQuery(document).ready(function () {
                    initializeNcElementsAndParams();
                })

                function initializeNcElementsAndParams() {
                    ncServerURLInput = jQuery("#acunetixServerURL");
                    ncApiTokenInput = jQuery("#acunetixApiToken");
                    ncPublicKeyInput = jQuery("#publicKey");
                    ncApiTokenInitialValueInput = jQuery("#acunetixApiTokenInitial");
                    ncEncryptedApiTokenInput = jQuery("#acunetixEncryptedApiToken");

                    ncTestConnectionResultSpan = jQuery("#acunetixConnectionResult");
                    ncTestConnectionButton = jQuery("#acunetixTestConnectionButton");

                    ncTestConnectionButton.click(ncTestConnection);
                    ncServerURLInput.attr('placeholder', "Cloud URL, like 'https://www.acunetix.com'");

                    ncTestRequestParams.ApiTokenInitialValue = ncApiTokenInput.val();
                    ncApiTokenInitialValueInput.val(ncApiTokenInput.val());

                    updateNcParams();
                }

                function updateNcParams() {
                    ncScanParams.serverURL = ncServerURLInput.val();
                    ncScanParams.apiToken = ncApiTokenInput.val();
                    ncScanParams.encryptedApiToken = ncEncryptedApiTokenInput.val();
                    if (ncScanParams.apiToken != ncTestRequestParams.ApiTokenInitialValue) {
                        ncScanParams.encryptedApiToken = "";
                        ncEncryptedApiTokenInput.val("");
                    }
                    ncTestRequestParams.acunetixServerURL = ncScanParams.serverURL;
                    ncTestRequestParams.acunetixApiToken = BS.Encrypt.encryptData(ncScanParams.apiToken, ncPublicKeyInput.val());
                    ncTestRequestParams.acunetixEncryptedApiToken = ncScanParams.encryptedApiToken;
                }

                function ncTestConnection() {
                    updateNcParams();
                    var request = jQuery.post("/acunetix/testconnection.html", ncTestRequestParams);

                    request.done(function (data, statusText, xhr) {
                        var status = jQuery(data).find("httpStatusCode").text();
                        if (status == "200") {
                            ncTestConnectionResultSpan.text("Successfully connected to the Acunetix 360.");
                        } else {
                            if (status == "0") {
                                ncTestConnectionResultSpan.text("Failed to connect to the Acunetix 360. HTTP status code: 0");
                            } else {
                                ncTestConnectionResultSpan.text("Acunetix 360 rejected the request. HTTP status code: " + status);
                            }
                        }
                    });

                    request.fail(function (xhr, statusText) {
                        ncTestConnectionResultSpan.text("Controller not found. HTTP status code: " + xhr.status);
                    });
                }
            </script>
            <div class="saveButtonsBlock" id="saveButtons" style="display:block">
                <forms:submit label="Save"></forms:submit>
                <a class="btn cancel" href="${pluginSettingsManager.cameFromSupport.cameFromUrl}">Cancel</a>
                <forms:saving/>
                <input type="hidden" value="0" name="numberOfSettingsChangesEvents">
                <input type="hidden" id="publicKey" name="publicKey"
                       value="${pluginSettingsManager.hexEncodedPublicKey}"/>
            </div>
        </form>
    </bs:refreshable>
</div>