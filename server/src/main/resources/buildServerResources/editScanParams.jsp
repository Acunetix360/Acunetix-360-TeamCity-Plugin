<%@ page import=" com.acunetix.teamcity.ScanRequest" %>
<%@ page import=" com.acunetix.teamcity.ScanType" %>
<%@include file="/include.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="serverTC" type="jetbrains.buildServer.serverSide.SBuildServer" scope="request"/>

<l:settingsGroup title="Scan Settings">
    <tr>
        <th>
            <label for="acunetixScanType">Scan Type:
                <l:star />
                <bs:helpIcon iconTitle="<b>Incremental</b>
                        <hr>
                        The website's profile is used for retrieving the scan settings.<br>
                        The last scan with the same scan setting will be used as a base for the incremental scan.<br><br>
                        <b>Full (With primary profile)</b>
                        <hr>
                        Performs full scan with primary profile.<br>
                        If no primary profile have been defined yet, default Acunetix 360 scan settings will be used.<br><br>
                        <b>Full (With selected profile)</b>
                        <hr>
                        Performs full scan with provided profile settings." />
            </label>
        </th>
        <td>
            <props:selectProperty id="acunetixScanType" name="<%=ScanRequest.SCAN_TYPE_Literal%>" className="longField">
                <props:option value="">-- Please select a scan type --</props:option>
                <props:option value="<%=ScanType.Incremental.name()%>">Incremental</props:option>
                <props:option value="<%=ScanType.FullWithPrimaryProfile.name()%>">Full (With primary profile)
                </props:option>
                <props:option value="<%=ScanType.FullWithSelectedProfile.name()%>">Full (With selected profile)
                </props:option>
            </props:selectProperty>
            <span class="error" id="error_acunetixScanType"></span>
        </td>
    </tr>
    <tr>
        <th>
            <label for="acunetixWebsiteID">Website Deploy URL:
                <l:star />
                <bs:helpIcon iconTitle="This address will be scanned." />
            </label>
        </th>
        <td>
            <props:hiddenProperty id="<%=ScanRequest.WEBSITE_ID_Literal%>" name="<%=ScanRequest.WEBSITE_ID_Literal%>" />
            <props:selectProperty id="acunetixWebsiteID_dummy" name="acunetixWebsiteID_dummy" enableFilter="true"
                className="longField">
                <props:option value="">-- Please select a website URL --</props:option>
            </props:selectProperty>
            <span class="error" id="error_acunetixWebsiteID"></span>
        </td>
    </tr>
    <tr>
        <th>
            <label for="acunetixProfileID">Profile Name:
                <l:star />
                <bs:helpIcon iconTitle="This profile setting will be used in the scan." />
            </label>
        </th>
        <td>
            <props:hiddenProperty id="<%=ScanRequest.PROFILE_ID_Literal%>" name="<%=ScanRequest.PROFILE_ID_Literal%>" />
            <props:selectProperty id="acunetixProfileID_dummy" name="acunetixProfileID_dummy" enableFilter="true"
                className="longField">
                <props:option value="">-- Please select a profile name --</props:option>
            </props:selectProperty>
            <span class="error" id="error_acunetixProfileID"></span>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <forms:progressRing style="display:none;float:left;" id="acunetixLoadingIcon"
                progressTitle="Loading the Acunetix 360 settings. Please wait..."></forms:progressRing>
            <span class="error" id="error_acunetixApiSettings"></span>
        </td>
    </tr>
</l:settingsGroup>
<script>
    //do not use $ for JQuery instead use jQuery
    var ncScanTypeInput, ncWebsiteIdInput, ncProfileIdInput;
    var ncWebsiteIdDummySelect, ncProfileIdDummySelect;
    var ncScanTypeContainer, ncWebsiteIdContainer, ncProfileIdContainer;
    var ncScanParams = {};
    var ncWebSiteModels = [];
    var ncTestRequestData = {};
    var ncInitialScanType, ncInitialWebsiteId, ncInitialProfileId;
    var acunetixApiSettingsErrorSpan;
    var acunetixLoadingIcon;

    function initializeNcElementsAndParams() {
        ncScanTypeInput = jQuery("#acunetixScanType");
        ncWebsiteIdInput = jQuery("#acunetixWebsiteID");
        ncProfileIdInput = jQuery("#acunetixProfileID");

        ncWebsiteIdDummySelect = jQuery("#acunetixWebsiteID_dummy");
        ncProfileIdDummySelect = jQuery("#acunetixProfileID_dummy");

        ncScanTypeContainer = ncScanTypeInput.closest("tr");
        ncWebsiteIdContainer = ncWebsiteIdInput.closest("tr");
        ncProfileIdContainer = ncProfileIdInput.closest("tr");

        acunetixApiSettingsErrorSpan = jQuery("#error_acunetixApiSettings");
        acunetixLoadingIcon = jQuery("#acunetixLoadingIcon");
        ncInitialScanType = ncScanTypeInput.val();
        ncInitialWebsiteId = ncWebsiteIdInput.val();
        ncInitialProfileId = ncProfileIdInput.val();

        ncScanTypeInput.change(ncScanTypeChanged);
        ncWebsiteIdDummySelect.change(ncWebsiteIdChanged);
        ncProfileIdDummySelect.change(ncProfileIdChanged);

        updateNcParamsAndUI();
        ncInitializeSettings();
    }

    function ncSelectInitialValues() {
        if (ncInitialScanType) {
            if (ncScanTypeInput.find("option[value='" + ncInitialScanType + "']").length > 0) {
                ncScanTypeInput.val(ncInitialScanType);
                ncScanTypeInput.change();
                if (ncInitialWebsiteId) {
                    if (ncWebsiteIdDummySelect.find("option[value='" + ncInitialWebsiteId + "']").length > 0) {
                        ncWebsiteIdDummySelect.val(ncInitialWebsiteId);
                        ncWebsiteIdDummySelect.change();
                        if (ncProfileIdDummySelect.find("option[value='" + ncInitialProfileId + "']").length > 0) {
                            ncProfileIdDummySelect.val(ncInitialProfileId);
                            ncProfileIdDummySelect.change();
                        }
                    }
                }
            }
        }
    }

    function updateNcParamsAndUI() {
        ncScanParams.scanType = ncScanTypeInput.val();
        if (!ncScanParams.scanType) {
            ncWebsiteIdDummySelect.val("");
        }

        ncWebsiteIdInput.val(ncWebsiteIdDummySelect.val());
        ncScanParams.websiteId = ncWebsiteIdInput.val();
        if (!ncScanParams.websiteId) {
            ncProfileIdDummySelect.val("");
        }

        ncProfileIdInput.val(ncProfileIdDummySelect.val());
        ncScanParams.profileId = ncProfileIdInput.val();

        ncRenderUIElements();
    }

    function ncRenderUIElements() {
        if (ncScanParams.scanType == "FullWithPrimaryProfile") {
            ncProfileIdContainer.hide();
        } else {
            ncProfileIdContainer.show();
        }
    }

    function ncInitializeSettings() {
        updateNcParamsAndUI();
        ncScanParams.isConnectionValidated = false;
        acunetixLoadingIcon.show();
        var request = jQuery.post("${serverTC.rootUrl}/acunetix/websitemodel.html");

        request.done(function (data, statusText, xhr) {
            ncTestRequestData = data;
            var status = jQuery(data).find("httpStatusCode").text();

            if (status == "200") {
                ncParseWebsiteData(data);
                ncScanParams.isConnectionValidated = true;
                ncInitializeWebsites();
                ncSelectInitialValues();
            } else {
                if (status == "0") {
                    acunetixApiSettingsErrorSpan.text("Failed to connect to the Acunetix 360. HTTP status code: 0");
                } else {
                    acunetixApiSettingsErrorSpan.text("API settings are not valid. Please verify API settings in Administration page. HTTP status code: " + status);
                }
            }
            acunetixLoadingIcon.hide();
        });

        request.fail(function (xhr, statusText) {
            acunetixApiSettingsErrorSpan.text("Controller not found. HTTP status code: " + xhr.status);
            acunetixLoadingIcon.hide();
        });
    }

    function ncParseWebsiteData(data) {
        var pluginWebsiteModels = jQuery(data).find("PluginWebsiteModels");
        pluginWebsiteModels.children().each(function (i, website) {
            var pluginWebsiteModel = jQuery(website);
            var WebsiteModel = {};

            WebsiteModel.Id = pluginWebsiteModel.children().filter("Id").text();
            WebsiteModel.Name = pluginWebsiteModel.children().filter("Name").text();
            WebsiteModel.Url = pluginWebsiteModel.children().filter("Url").text();

            WebsiteModel.Profiles = [];

            var pluginWebsiteModels = pluginWebsiteModel.find("WebsiteProfiles");
            pluginWebsiteModels.children().each(function (j, profile) {
                var pluginProfileModel = jQuery(profile);
                var profileModel = {};

                profileModel.Id = pluginProfileModel.children().filter("Id").text();
                profileModel.Name = pluginProfileModel.children().filter("Name").text();

                WebsiteModel.Profiles[j] = profileModel
            });

            ncWebSiteModels[i] = WebsiteModel;
        });

        return ncWebSiteModels;
    }

    function ncInitializeWebsites() {
        updateNcParamsAndUI();
        var dummyListElement = document.getElementsByClassName("list-wrapper-ufd-teamcity-ui-prop:acunetixWebsiteID_dummy");
        var ncWebsiteIdDummyList = jQuery(dummyListElement).find("ul").first();
        ncResetWebsiteOptions(ncWebsiteIdDummyList);
        ncAppendWebsiteOptions(ncWebsiteIdDummyList);
        bootstrapDropdownToList('acunetixWebsiteID_dummy');
        ncSelectDefaultScanType();
    }

    function ncResetWebsiteOptions(dummyList) {
        ncWebsiteIdDummySelect.find('option').remove();
        dummyList.find('li').remove();

        ncWebsiteIdDummySelect
            .append(jQuery("<option></option>")
                .attr("value", "")
                .text("-- Please select a website URL --"));

        dummyList.append(jQuery("<li></li>")
            .attr("class", "option nogroup active")
            .attr("name", 0)
            .attr("data-title", "-- Please select a website URL --")
            .text("-- Please select a website URL --"));
    }

    function ncAppendWebsiteOptions(dummyList) {
        jQuery.each(ncWebSiteModels, function (index, webSiteModel) {
            var websiteText = webSiteModel.Name + " (" + webSiteModel.Url + ")";

            ncWebsiteIdDummySelect
                .append(jQuery("<option></option>")
                    .attr("value", webSiteModel.Id)
                    .text(websiteText));

            dummyList.append(jQuery("<li></li>")
                .attr("class", "option nogroup")
                .attr("name", index + 1)
                .attr("data-title", websiteText)
                .text(websiteText));
        });
    }

    function bootstrapDropdownToList(dummy_selector) {
        BS.jQueryDropdown($(dummy_selector), {});
        $(dummy_selector).setSelected = function (idx) {
            $(dummy_selector).selectedIndex = idx;
            BS.jQueryDropdown($(dummy_selector)).ufd("changeOptions");
        };
        $(dummy_selector).setSelectValue = function (val) {
            var idx = 0;
            var selector = $(dummy_selector);
            for (var i = 0; i < selector.options.length; i++) {
                if (selector.options[i].value == val) {
                    idx = i;
                    break;
                }
            }

            selector.setSelected(idx);
        };
    }

    function ncSelectDefaultScanType() {
        if (ncInitialScanType) {
            if (ncScanTypeInput.find("option[value='" + ncInitialScanType + "']").length > 0) {
                ncScanTypeInput.val(ncInitialScanType);
                ncScanTypeInput.change();
            } else {
                ncScanTypeInput.prop("selectedIndex", 0).change();
            }
        } else {
            ncScanTypeInput.prop("selectedIndex", 0).change();
        }
    }

    function ncScanTypeChanged() {
        updateNcParamsAndUI();
        ncInitialScanType = ncScanTypeInput.val();
    }

    function ncWebsiteIdChanged() {
        updateNcParamsAndUI();
        ncInitialWebsiteId = ncWebsiteIdInput.val();
        if (ncScanParams.websiteId) {
            ncInitializeProfiles();
        }
    }

    function ncFindModelIndexesFromSelectValues() {
        var modelIndexes = {};
        modelIndexes.websiteIndex = -1;
        modelIndexes.profileIndex = -1;

        var websiteID = ncWebsiteIdDummySelect.val();
        if (websiteID) {
            for (var i = 0; i < ncWebSiteModels.length; ++i) {
                var websiteModel = ncWebSiteModels[i];
                if (websiteModel.Id == websiteID) {
                    modelIndexes.websiteIndex = i;
                    break;
                }
            }
        }

        if (modelIndexes.websiteIndex != -1) {
            var profileID = ncProfileIdDummySelect.val();
            if (profileID) {
                var profileModels = ncWebSiteModels[modelIndexes.websiteIndex].Profiles;
                for (var j = 0; j < profileModels.length; ++j) {
                    var profileModel = profileModels[j];
                    if (profileModel.Id == profileID) {
                        modelIndexes.profileIndex = j;
                        break;
                    }
                }
            }
        }

        return modelIndexes;
    }

    function ncInitializeProfiles() {
        updateNcParamsAndUI();
        var websiteIndex = ncFindModelIndexesFromSelectValues().websiteIndex;
        if (websiteIndex != -1) {
            var ncProfileModels = ncWebSiteModels[websiteIndex].Profiles;
            var dummyListElement = document.getElementsByClassName("list-wrapper-ufd-teamcity-ui-prop:acunetixProfileID_dummy");
            var ncProfileIdDummyList = jQuery(dummyListElement).find("ul").first();
            ncResetProfileOptions(ncProfileIdDummyList, ncProfileModels.length);
            ncAppendProfileOptions(ncProfileIdDummyList, ncProfileModels);
            bootstrapDropdownToList('acunetixProfileID_dummy');
            ncSelectDefaultProfile();
        }
    }

    function ncResetProfileOptions(dummyList, profileCount) {
        var placeholderText;
        if (profileCount > 0) {
            placeholderText = "-- Please select a profile name --";
        } else {
            placeholderText = "-- No profile found --"
        }

        ncProfileIdDummySelect.find('option').remove();
        dummyList.find('li').remove();

        ncProfileIdDummySelect
            .append(jQuery("<option></option>")
                .attr("value", "")
                .text(placeholderText));

        dummyList.append(jQuery("<li></li>")
            .attr("class", "option nogroup active")
            .attr("name", 0)
            .attr("data-title", placeholderText)
            .text(placeholderText));
    }

    function ncAppendProfileOptions(dummyList, ncProfileModels) {
        jQuery.each(ncProfileModels, function (index, profileModel) {
            var profileText = profileModel.Name;
            ncProfileIdDummySelect
                .append(jQuery("<option></option>")
                    .attr("value", profileModel.Id)
                    .text(profileText));

            dummyList.append(jQuery("<li></li>")
                .attr("class", "option nogroup")
                .attr("name", index + 1)
                .attr("data-title", profileText)
                .text(profileText));
        });
    }

    function ncSelectDefaultProfile() {
        if (ncInitialProfileId) {
            if (ncProfileIdDummySelect.find("option[value='" + ncInitialProfileId + "']").length > 0) {
                ncProfileIdDummySelect.val(ncInitialProfileId);
                ncProfileIdDummySelect.change();
            } else {
                ncProfileIdDummySelect.prop("selectedIndex", 0).change();
            }
        } else {
            ncProfileIdDummySelect.prop("selectedIndex", 0).change();
        }
    }

    function ncProfileIdChanged() {
        updateNcParamsAndUI();
        ncInitialProfileId = ncProfileIdInput.val();
    }

    //do noy use $ for Jquery instead use jQuery
    jQuery(document).ready(function () {
        initializeNcElementsAndParams();
    })
</script>