<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Packages</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Packages</h2>
<br>
<h3>Packages requested from GetTor</h3>
<br>
<p>GetTor allows users to fetch the Tor software via email. The following
graph shows the number of packages requested from GetTor per day.</p>
<p>
<a name="gettor"></a>
<img src="gettor.png${gettor_url}"
     width="576" height="360" alt="GetTor graph">
<form action="packages.html#gettor">
  <div class="formrow">
    <input type="hidden" name="graph" value="gettor">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${gettor_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${gettor_end[0]}">
    </p><p>
      Packages:
      <input type="radio" name="bundle" value="all"> Total packages
      <input type="radio" name="bundle" value="en"> TBB (en)
      <input type="radio" name="bundle" value="zh_CN"> TBB (zh_CN)
      <input type="radio" name="bundle" value="fa"> TBB (fa)
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>

<p><a href="csv/gettor.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
