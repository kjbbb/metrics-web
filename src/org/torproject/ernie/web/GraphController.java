package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import org.rosuda.REngine.Rserve.*;
import org.rosuda.REngine.*;

public class GraphController {

  private static final String baseDir;
  private static final int cacheSize;
  private final String graphName;

  static {
    ErnieProperties props = new ErnieProperties();
    cacheSize = props.getInt("max.cached.graphs");
    baseDir = props.getProperty("cached.graphs.dir");

    try {
      /* Create temp graphs directory if it doesn't exist. */
      File dir = new File(baseDir);
      if (!dir.exists())  {
        dir.mkdirs();
      }

      /* Change directory permissions to allow it to be written to
       * by Rserve. */
      Runtime rt = Runtime.getRuntime();
      rt.exec("chmod 777 " + baseDir).waitFor();
    } catch (InterruptedException e) {
    } catch (IOException e) {}
  }

  public GraphController (String graphName)  {
    this.graphName = graphName;
  }

  public void writeOutput(String imagePath, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    /* Read file from disk and write it to response. */
    BufferedInputStream input = null;
    BufferedOutputStream output = null;
    try {
      File imageFile = new File(imagePath);
      /* If there was an error when generating the graph,
       * set the header to 404 not found. */
      if (!imageFile.exists())  {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else {
        response.setContentType("image/png");
        response.setHeader("Content-Length", String.valueOf(
            imageFile.length()));
        response.setHeader("Content-Disposition",
            "inline; filename=\"" + graphName + ".png" + "\"");
        input = new BufferedInputStream(new FileInputStream(imageFile),
            1024);
        output = new BufferedOutputStream(response.getOutputStream(), 1024);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
      }
    }
    finally {
      if (output != null)
        output.close();
      if (input != null)
        input.close();
    }
  }

  public void generateGraph(String rquery)  {
    /* Send request to Rserve. */
    try {
      RConnection rc = new RConnection();
      rc.eval(rquery);
      rc.close();
    } catch (Exception e) {
    }
  }

  /* Caching mechanism to delete the least recently
   * used graph. */
  public void deleteLRUgraph()  {
    //TODO
    File dir = new File(baseDir);
    List<File> flist = Arrays.asList(dir.listFiles());
    if (flist.size() > cacheSize)  {
      Collections.sort(flist);
      flist.get(0).delete();
    }
  }

  public String getBaseDir()  {
    return this.baseDir;
  }
}

