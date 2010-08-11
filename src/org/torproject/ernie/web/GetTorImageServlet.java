package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

public class GetTorImageServlet extends HttpServlet {

  private static final Logger log;
  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private final Constants c;
  private static final SortedSet<String> validBundles;

  static {
    log = Logger.getLogger(GetTorImageServlet.class);
    ErnieProperties props = new ErnieProperties();
    validBundles = new TreeSet<String>(Arrays.asList(
        props.getProperty("gettor.bundles").split(",")));
  }

  public GetTorImageServlet()  {
    this.graphName = "gettor";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_gettor_line('%s', '%s', '%s', '%s')";
    this.c = new Constants();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query, bundle;

      start = request.getParameter("start");
      end = request.getParameter("end");
      bundle = request.getParameter("bundle");

      /* Validate input */
      if (start == null || end == null || bundle == null ||
          !validBundles.contains(bundle)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      try {
        simpledf.parse(start);
        simpledf.parse(end);
      } catch (ParseException e)  {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" +
          end + "-" + bundle);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, bundle);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e)  {
      log.warn(e.toString());
    } catch (IOException e) {
      log.warn(e.toString());
    }
  }
}
