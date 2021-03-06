<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Performance</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Performance</h2>
<br>
<h3>Time to download files over Tor</h3>
<br>
<p>The following graphs show the performance of the Tor network as
experienced by its users. The graphs contain the average (median) time to
request files of three different sizes over Tor as well as first and third
quartile of request times.</p>
<a name="torperf"></a>
<img src="torperf.png${torperf_url}"
     width="576" height="360" alt="Torperf graph">
<form action="performance.html#torperf">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${torperf_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${torperf_end[0]}">
    </p><p>
      Source:
      <input type="radio" name="source" value="torperf"> torperf
      <input type="radio" name="source" value="moria"> moria
      <input type="radio" name="source" value="siv"> siv
    </p><p>
      <label>File size: </label>
      <input type="radio" name="filesize" value="50kb"> 50 KiB
      <input type="radio" name="filesize" value="1mb"> 1 MiB
      <input type="radio" name="filesize" value="5mb"> 5 MiB
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>

<p><a href="csv/torperf.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
