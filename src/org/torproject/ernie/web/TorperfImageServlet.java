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
  private final Constants c;
  private static final SortedSet<String> validSources;
  private static final SortedSet<String> validSizes;

  static {
    log = Logger.getLogger(TorperfImageServlet.class);
    ErnieProperties props = new ErnieProperties();
    validSources = new TreeSet<String>(Arrays.asList(
        props.getProperty("torperf.sources").split(",")));
    validSizes = new TreeSet<String>(Arrays.asList(
        props.getProperty("torperf.sizes").split(",")));
  }

  public TorperfImageServlet()  {
    this.graphName = "torperf";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_torperf_line('%s', '%s', '%s', '%s', '%s')";
    this.c = new Constants();
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
      c.simpledf.parse(start);
      c.simpledf.parse(end);

      /* Set 404 if the user entered invalud/null source and
       * size parameters */
      if (!validSources.contains(source) ||
          !validSizes.contains(size)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" + end
          + "-" + source + "-" + size);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, source, size);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e) {
    } catch (ParseException e) {
    } catch (IOException e) {
    }
  }
}
