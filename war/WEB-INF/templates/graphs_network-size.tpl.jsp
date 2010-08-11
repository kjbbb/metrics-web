<jsp:useBean id="dateranges" class="org.torproject.ernie.util.DateRanges" scope="request"/>
<%@page import="java.util.*" %>
<%@page import="java.io.*" %>
        <h2>Tor Metrics Portal: Graphs</h2>
        <br/>
        <h3>Relays and bridges in the Tor network</h3>
        <br/>
        <p>The number of relays and bridges in the Tor network can be
        extracted from the hourly published network status consensuses
        and sanitized bridge statuses.</p>
        <ul>
          <li>Past <a href="#networksize-30d">30</a>,
              <a href="#networksize-90d">90</a>,
              <a href="#networksize-180d">180</a> days</li>
          <li><a href="#networksize-all">All data</a> up to today</li>
          <%int[] years = dateranges.getYearsRange();%>
          <li>Annual graphs of <%for(int i = 0; i < years.length; i++) {%>
            <a href="#networksize-<%=years[i]%>"><%=years[i]%></a>, <%}%></li>
        </ul>

<p>Network size past 30 days.
  <a id="networksize-30d"/></p>
  <img src="/networksize.png?start=<%=dateranges.getDayRange(30)[0]%>&end=<%=dateranges.getDayRange(30)[1]%>"/>
<p>Network size past 90 days.
  <a id="networksize-90d"/></p>
  <img src="/networksize.png?start=<%=dateranges.getDayRange(90)[0]%>&end=<%=dateranges.getDayRange(90)[1]%>"/>
<p>Network size past 180 days.
  <a id="networksize-180d"/></p>
  <img src="/networksize.png?start=<%=dateranges.getDayRange(180)[0]%>&end=<%=dateranges.getDayRange(180)[1]%>"/>
<p>Network size (All data).
  <a id="networksize-all"/></p>
  <img src="/networksize.png?start=<%=dateranges.getAllDataRange()[0]%>&end=<%=dateranges.getAllDataRange()[1]%>"/>

<%for (int i = 0; i < years.length; i++) {%>
<p>Network size (<%=years[i]%>)
  <a id="networksize-<%=years[i]%>"/></p>
  <img src="/networksize.png?start=<%=dateranges.getYearsRangeDates(
      years[i])[0]%>&end=<%=dateranges.getYearsRangeDates(
      years[i])[1]%>"/>
<%}%>
