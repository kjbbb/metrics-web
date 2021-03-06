package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Controller servlet for the Data page. Prepares the various lists of
 * downloadable metrics data files by parsing a file with URLs on other
 * servers and looking at a local directory with files served by local
 * Apache HTTP server. The file with URLs on other servers may contain
 * comment lines starting with #. Recognizes metrics data file types from
 * the file names.
 */
public class ResearchDataServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Read file with URLs of metrics data files on other servers. */
    List<String> dataFileUrls = new ArrayList<String>();
    String remoteDataFiles = getServletConfig().getInitParameter(
        "remoteDataFiles");
    if (remoteDataFiles != null) {
      try {
        File remoteDataFilesFile = new File(remoteDataFiles);
        if (remoteDataFilesFile.exists() &&
            !remoteDataFilesFile.isDirectory()) {
          BufferedReader br = new BufferedReader(new FileReader(
              remoteDataFilesFile));
          String line = null;
          while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
              dataFileUrls.add(line);
            }
          }
        }
      } catch (SecurityException e) {
        /* We're not permitted to read the file with URLs. Ignore. */
      }
    }

    /* Read local directory with files served by the local Apache HTTP
     * server and add the URLs to the list. */
    String localDataDir = getServletConfig().getInitParameter(
        "localDataDir");
    if (localDataDir != null) {
      try {
        File localDataDirFile = new File(localDataDir);
        if (localDataDirFile.exists() && localDataDirFile.isDirectory()) {
          for (File localDataFile : localDataDirFile.listFiles()) {
            if (!localDataFile.isDirectory()) {
              dataFileUrls.add("/data/" + localDataFile.getName());
            }
          }
        }
      } catch (SecurityException e) {
        /* We're not permitted to read the directory with metrics data
         * files. Ignore. */
      }
    }

    /* Prepare data structures that we're going to pass to the JSP. All
     * data structures are (nested) maps with the map keys being used for
     * displaying the files in tables and map values being 2-element
     * arrays containing the file url and optional signature file. */
    SortedMap<Date, Map<String, String[]>> relayDescriptors =
        new TreeMap<Date, Map<String, String[]>>();
    SortedMap<Date, String[]> bridgeDescriptors =
        new TreeMap<Date, String[]>();
    SortedMap<String, Map<String, String[]>> relayStatistics =
        new TreeMap<String, Map<String, String[]>>();
    SortedMap<String, Map<String, String[]>> torperfData =
        new TreeMap<String, Map<String, String[]>>();
    SortedMap<Date, String[]> exitLists = new TreeMap<Date, String[]>();

    /* Go through the file list, decide for each file what metrics data
     * type it is, and put it in the appropriate map. */
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
    List<String> torperfFilesizes = Arrays.asList("50kb,1mb,5mb".
        split(","));
    for (String url : dataFileUrls) {
      if (!url.contains("/")) {
        continue;
      }
      String filename = url.substring(url.lastIndexOf("/") + 1);

      /* URL contains relay descriptors. */
      if (filename.startsWith("tor-20") ||
          filename.startsWith("statuses-20") ||
          filename.startsWith("server-descriptors-20") ||
          filename.startsWith("extra-infos-20") ||
          filename.startsWith("votes-20") ||
          filename.startsWith("consensuses-20")) {
        String type = filename.substring(0, filename.indexOf("-20"));
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        int index = filename.endsWith(".asc") ? 1 : 0;
        if (!relayDescriptors.containsKey(month)) {
          relayDescriptors.put(month, new HashMap<String, String[]>());
        }
        if (!relayDescriptors.get(month).containsKey(type)) {
          relayDescriptors.get(month).put(type, new String[2]);
        }
        relayDescriptors.get(month).get(type)[index] = url;

      /* URL contains bridge descriptors. */
      } else if (filename.startsWith("bridge-descriptors-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        int index = filename.endsWith(".asc") ? 1 : 0;
        if (!bridgeDescriptors.containsKey(month)) {
          bridgeDescriptors.put(month, new String[2]);
        }
        bridgeDescriptors.get(month)[index] = url;

      /* URL contains relay statistics. */
      } else if (filename.startsWith("buffer-") ||
          filename.startsWith("dirreq-") ||
          filename.startsWith("entry-") ||
          (filename.startsWith("exit-") &&
          !filename.startsWith("exit-list-"))) {
        String[] parts = filename.split("-");
        if (parts.length != 3) {
          continue;
        }
        String type = parts[0];
        String nickname = parts[1];
        String fingerprint = parts[2];
        fingerprint = fingerprint.substring(0, 8);
        int index = filename.endsWith(".asc") ? 1 : 0;
        String nicknameAndFingerprint = nickname + " ("
            + fingerprint.toUpperCase() + ")";
        if (!relayStatistics.containsKey(nicknameAndFingerprint)) {
          relayStatistics.put(nicknameAndFingerprint,
              new HashMap<String, String[]>());
        }
        if (!relayStatistics.get(nicknameAndFingerprint).containsKey(
            type)) {
          relayStatistics.get(nicknameAndFingerprint).put(type,
              new String[2]);
        }
        relayStatistics.get(nicknameAndFingerprint).get(type)[index] =
            url;

      /* URL contains Torperf data file. */
      } else if (filename.endsWith("b.data")) {
        String[] parts = filename.split("-");
        if (parts.length != 2) {
          continue;
        }
        String source = parts[0];
        String filesize = parts[1];
        filesize = filesize.substring(0, filesize.length() - 5);
        if (!torperfFilesizes.contains(filesize)) {
          continue;
        }
        if (!torperfData.containsKey(source)) {
          torperfData.put(source, new HashMap<String, String[]>());
        }
        if (!torperfData.get(source).containsKey(filesize)) {
          torperfData.get(source).put(filesize, new String[2]);
        }
        torperfData.get(source).get(filesize)[0] = url;

      /* URL contains exit list. */
      } else if (filename.startsWith("exit-list-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        if (!exitLists.containsKey(month)) {
          exitLists.put(month, new String[2]);
        }
        exitLists.get(month)[0] = url;
      }
    }

    /* Add the maps to the request and forward it to the JSP to display
     * the page. */
    request.setAttribute("relayDescriptors", relayDescriptors);
    request.setAttribute("bridgeDescriptors", bridgeDescriptors);
    request.setAttribute("relayStatistics", relayStatistics);
    request.setAttribute("torperfData", torperfData);
    request.setAttribute("exitLists", exitLists);
    request.getRequestDispatcher("WEB-INF/data.jsp").forward(request,
        response);
  }
}

