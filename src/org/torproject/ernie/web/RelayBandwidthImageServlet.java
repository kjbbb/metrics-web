package org.torproject.ernie.web;

import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;

public class RelayBandwidthImageServlet extends HttpServlet {

  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private final Constants c;

  public RelayBandwidthImageServlet()  {
    this.graphName = "bandwidth";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_bandwidth_line('%s', '%s', '%s')";
    this.c = new Constants();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query;

      start = request.getParameter("start");
      end = request.getParameter("end");

      /* Validate input */
      c.simpledf.parse(start);
      c.simpledf.parse(end);

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" + end);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);
      gcontroller.deleteLRUgraph();

    } catch (NullPointerException e) {
    } catch (ParseException e) {
    } catch (IOException e) {
    }
  }
}
