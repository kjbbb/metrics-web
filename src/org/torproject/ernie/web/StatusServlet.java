package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class StatusServlet extends HttpServlet {
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/status.jsp").forward(request,
        response);
  }
}

