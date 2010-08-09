package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;

public class GetTorImageServlet extends HttpServlet {

  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private final Constants c;
  private static final SortedSet<String> validBundles;

  static {
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
      c.simpledf.parse(start);
      c.simpledf.parse(end);

      if (!validBundles.contains(bundle)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
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

    } catch (ParseException e)  {
    } catch (NullPointerException e)  {
    } catch (IOException e) {
    }
  }
}
