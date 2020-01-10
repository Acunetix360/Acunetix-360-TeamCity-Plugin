<%@ page import=" com.acunetix.teamcity.ScanRequest" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="parameter">
    NC Server URL:<strong><props:displayValue name="<%=ScanRequest.API_URL_Literal%>" emptyValue="none"/></strong>
</div>
<div class="parameter">
    Api Token:<strong><props:displayValue name="<%=ScanRequest.API_TOKEN_Literal%>" emptyValue="none"/></strong>
</div>
<div class="parameter">
    Scan Type:<strong><props:displayValue name="<%=ScanRequest.SCAN_TYPE_Literal%>" emptyValue="none"/></strong>
</div>


