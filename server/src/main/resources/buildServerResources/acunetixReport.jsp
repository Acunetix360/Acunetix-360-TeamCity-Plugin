<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div>
    <p id="acunetixScanResultWarning"></p>
    <iframe id="acunetixScanResult" style="display:none;width:100%;height:70vh;"></iframe>
    <script id="acunetixScanResultContent" type="text/html">
        ${content}
    </script>
    <script>
    jQuery(document).ready(function () {
        var isReportGenerated =  ${isReportGenerated};
        var content=jQuery('#acunetixScanResultContent').html();
        var hasError=${hasError};
        var errorMessage="${errorMessage}";
        var warning=jQuery('#acunetixScanResultWarning');
        var iframe = document.getElementById('acunetixScanResult');


        if(hasError){
            warning.text(errorMessage);
            jQuery('#acunetixScanResult').hide();
            warning.show();
        }
        else if(isReportGenerated){
            iframe = iframe.contentWindow || ( iframe.contentDocument.document || iframe.contentDocument);
            iframe.document.open();
            iframe.document.write(content);
            iframe.document.close();
            jQuery('#acunetixScanResult').show();
            warning.hide();
        }else{
            warning.text(content);
            jQuery('#acunetixScanResult').hide();
            warning.show();
        }
    });
    </script>
</div>

