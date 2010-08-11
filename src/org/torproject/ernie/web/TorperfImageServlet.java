package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;

public class TorperfImageServlet extends HttpServlet {

  private static final Logger log;
  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private final SimpleDateFormat simpledf;
  private static final Set<String> validSources;
  private static final Set<String> validSizes;

  static {
    log = Logger.getLogger(TorperfImageServlet.class);
    ErnieProperties props = new ErnieProperties();
    validSources = new HashSet<String>(Arrays.asList(
        props.getProperty("torperf.sources").split(",")));
    validSizes = new HashSet<String>(Arrays.asList(
        props.getProperty("torperf.sizes").split(",")));
  }

  public TorperfImageServlet()  {
    this.graphName = "torperf";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_torperf_line('%s', '%s', '%s', '%s', '%s')";
    this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
    this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query, source, size;

      start = request.getParameter("start");
      end = request.getParameter("end");
      source = request.getParameter("source");
      size = request.getParameter("size");

      /* Validate input */
      if (start == null || end == null || source == null ||
          size == null || !validSources.contains(source) ||
          !validSizes.contains(size))  {
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

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" + end
          + "-" + source + "-" + size);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, source, size);
      gcontroller.generateGraph(query, path);
      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e) {
      log.warn(e.toString());
    } catch (IOException e) {
      log.warn(e.toString());
    }
  }
}
