package org.torproject.ernie.web;

import java.util.*;
import java.text.*;

public class CustomGraphController  {

  private Map<String, String[]> parameterMap;
  private Set<String> error;
  private String graphURI;
  private static SimpleDateFormat simpledf;

  static {
    simpledf = new SimpleDateFormat("yyyy-MM-dd");
    simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Default constructor. */
  public CustomGraphController() {
    this.error = new HashSet<String>();
  }

  public void setParameterMap(Map<String, String[]> parameterMap) {
    this.parameterMap = parameterMap;
  }

  /**
   * Build a copy of the parameters passed to custom-graph.jsp to be
   * passed to one of the graph image servlets.
   * */
  public String getGraphURL() {
    try {
      Map<String, String[]> mapCopy = new HashMap(parameterMap);
      String graphURI = "/" + mapCopy.get("graph")[0] + ".png";

      /*Make sure we don't pass this key/value to the servlet. */
      mapCopy.remove("graph");

      /* Make sure the user entered a valid date */
      simpledf.parse(mapCopy.get("start")[0]);
      simpledf.parse(mapCopy.get("end")[0]);

      int i = 0;
      String uriChar;

      for (Map.Entry<String, String[]> entry : mapCopy.entrySet()) {
        uriChar = (i == 0) ? "?" : "&";
        graphURI += (uriChar + entry.getKey() + "=" + entry.getValue()[0]);
        i++;
      }
      return graphURI;
      /* All of the forms are empty */
    } catch (NullPointerException e) {
      error.add("Invalid date format.");
      return "";
    } catch (ParseException e)  {
      error.add("Invalid date format.");
      return "";
    }
  }

  public String getGraphName()  {
    try {
      return parameterMap.get("graph")[0];
    } catch (NullPointerException e)  {
      return "error";
    }
  }
  public String getGraphStart() {
    try {
      return parameterMap.get("start")[0];
    } catch (NullPointerException e)  {
      return "error";
    }
  }
  public String getGraphEnd() {
    try {
      return parameterMap.get("end")[0];
    } catch (NullPointerException e) {
      return "error";
    }
  }
  public Set<String> getError() {
    return this.error;
  }
}
