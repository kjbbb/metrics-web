package org.torproject.ernie.web;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.sql.Date;
import java.text.*;

public class ExoneraTorController {

  private Constants c;

  private Connection conn;

  private PreparedStatement psRelays;

  private SimpleDateFormat dfmt;

  private HashSet<String> error;

  private String address;

  private String validAfter;

  private String ipRegex;

  public ExoneraTorController() {
    this.error = new HashSet<String>();
    this.dfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    try {
      this.c = new Constants();
      this.conn = DriverManager.getConnection(c.jdbcURL);
      this.psRelays= conn.prepareStatement("select * from " +
          "descriptor_statusentry where address like '?' " +
          "and validafter >= '?' and validafter <= '?'");
      this.ipRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
          "\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    } catch (SQLException e)  {
      setError("Internal database error: Could not connect to database");
    }
  }

  public void setAddress(String address)  {
    this.address = address;
  }

  public void setValidAfter(String validAfter)  {
    this.validAfter = validAfter;
  }
  public Map<String, String> getResults() {

    HashMap<String, String> relays = new HashMap<String, String>();

    /* Check if IP address matches regular expression */
    if (!Pattern.matches("ipRegex", address)) {
      setError("Enter a valid IP address.");
    }

    try {

      ResultSet rs;
      Calendar min = Calendar.getInstance();
      Calendar max = Calendar.getInstance();
      min.setTimeZone(TimeZone.getTimeZone("UTC"));
      max.setTimeZone(TimeZone.getTimeZone("UTC"));

      java.util.Date dValidAfter = dfmt.parse(validAfter);
      max.setTime(dValidAfter);
      min = (Calendar)max.clone();
      min.add(Calendar.HOUR, -3);

      psRelays.setString(1, address);
      psRelays.setTime(2, new Time(min.getTime().getTime()));
      psRelays.setTime(3, new Time(max.getTime().getTime()));

      rs = psRelays.executeQuery();

      while (rs.next()) {
        Date validafter;
        String descriptor;
        validafter = rs.getDate("validafter");
        descriptor = rs.getString("descriptor");
        relays.put(validafter.toString(), descriptor);
      }

      return relays;

    } catch (SQLException e) {
      setError("Internal database error.");
    } catch (ParseException e)  {
      setError("Enter a valid timestamp.");
    }
    setError("No results");
    return null;
  }

  private void setError(String error) {
    this.error.add(error);
  }

  public HashSet<String> getError() {
    return this.error;
  }
}
