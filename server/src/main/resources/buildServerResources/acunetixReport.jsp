<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div>
    <p id="acunetixScanResultWarning"></p>
    <iframe id="acunetixScanResult" style="display:none;width:100%;height:70vh;" srcdoc="${content}"></iframe>
    <script>
        jQuery(document).ready(function () {
            var isReportGenerated = "${isReportGenerated}";
            var hasError = "${hasError}";
            var content = jQuery('#acunetixScanResultContent').html();
            var errorMessage = "${errorMessage}";
            var warning = jQuery('#acunetixScanResultWarning');
            var iframe = document.getElementById('acunetixScanResult');

            if (hasError == 'true') {
                warning.html(errorMessage);
                jQuery('#acunetixScanResult').hide();
                warning.show();
            }
            else if (isReportGenerated == 'true') {
                jQuery('#acunetixScanResult').show();
                warning.hide();
            } else {
                warning.html(content);
                jQuery('#acunetixScanResult').hide();
                warning.show();
            }
        });
    </script>
</div>