<jsp:useBean id="template" class="org.torproject.ernie.web.TemplateController" scope="request" />
<jsp:useBean id="dateranges" class="org.torproject.ernie.util.DateRanges" scope="request" />
<jsp:setProperty name="template" property="template" value="graphs_network-size"/>
<jsp:setProperty name="template" property="title" value="Tor Metrics Portal: Graphs"/>
<%@ include file="/WEB-INF/templates/main.tpl.jsp" %>
