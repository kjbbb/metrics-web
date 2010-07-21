package org.torproject.ernie.util;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.util.Date; /* Use java date instead of sql.*/

public class DateRanges {

  private static final Logger log;
  private static SimpleDateFormat simpledf;
  private final static String jdbcURL;
  private static final ErnieProperties props;

  private Connection conn;
  private PreparedStatement psYearsRange;
  private PreparedStatement psAllRange;

  static {
    log = Logger.getLogger(DateRanges.class.toString());
    props = new ErnieProperties();
    simpledf = new SimpleDateFormat("yyyy-MM-dd");
    simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
    jdbcURL = props.getProperty("jdbc.url");
  }

  public DateRanges() {
    try {
      this.conn = DriverManager.getConnection(jdbcURL);

      /* Its much faster to get the year from the aggregate tables instead
       * of the large statusentry or descriptor tables. TODO do this more
       * robustly? */
      this.psYearsRange = conn.prepareStatement(
          "select min(extract('year' from date(date))) as min, " +
          "max(extract('year' from date(date))) as max " +
          "from network_size");
      this.psAllRange = conn.prepareStatement(
          "select min(date(date)) as min, " +
          "max(date(date)) as max " +
          "from network_size");
    } catch (SQLException e)  {
      log.warn("Couldn't connect to database or prepare statements. " + e);
    }
  }

  /**
   * Get a range for days in the past, which returns a tuple
   * like (start, yyyy-mm-dd) and (end, yyyy-mm-dd).
   */
  public Map<String, String> getDayRange(int days)  {
    Map<String, String> dates = new HashMap<String, String>();
    Calendar today = Calendar.getInstance();
    today.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar start = (Calendar)today.clone();
    start.add(Calendar.DATE, -days);

    dates.put("start", simpledf.format(start.getTime()));
    dates.put("end", simpledf.format(today.getTime()));
    return dates;
  }

  /**
   * Get the years range (of current data in the database), which returns
   * a structure that looks like (year (start, end)), or (yyyy (yyyy-mm-dd, yyyy-mm-dd)).
   */
  public Map<Integer, Map<String, String>> getYearsRange()  {
    Map<Integer, Map<String, String>> yearsrange =
        new HashMap<Integer, Map<String, String>>();
    int min = 0, max = 0;
    try {
      ResultSet rsYearsRange = psYearsRange.executeQuery();
      if (rsYearsRange.next())  {
        min = rsYearsRange.getInt("min");
        max = rsYearsRange.getInt("max");
      }
      for (int year = min; year <= max; year++) {
        Map<String, String> dates = new HashMap<String, String>();
        dates.put(year + "01-01",year + "12-31");
        yearsrange.put(year, dates);
      }
    } catch (SQLException e) {
      log.warn("Couldn't get results from network_size table: " + e);
    }
    return yearsrange;
  }

  /**
   * Get the date range (of the current data in the database), which
   * returns a map with one row that contains the start and end dates
   * like (yyyy-mm-dd, yyyy-mm-dd).
   */
  public Map<String, String> getAllDataRange() {
    Map<String, String> range = new HashMap<String, String>();
    try {
      ResultSet rsAllRange = psAllRange.executeQuery();
      if (rsAllRange.next())  {
        range.put(rsAllRange.getString("min"),
            rsAllRange.getString("max"));
      }
    } catch (SQLException e) {
      log.warn("Couldn't get results from network_size table: " + e);
    }
    return range;
  }

  /**
   * Close database connection.
   */
  public void closeConnection() {
    try {
      this.conn.close();
    } catch (SQLException e)  {
      log.warn("Couldn't close database connection. " + e);
    }
  }
}
