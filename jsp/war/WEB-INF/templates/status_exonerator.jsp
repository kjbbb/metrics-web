        <h2>ExoneraTor</h2>
        <h3>or: a website that tells you whether some IP address was a Tor relay</h3>
        <p>ExoneraTor tells you whether there was a Tor relay running on a given
        IP address at a given time. ExoneraTor can further find out whether this relay
        permitted exiting to a given server and/or TCP port. ExoneraTor learns about
        these facts from parsing the public relay lists and relay descriptors that are
        collected from the Tor directory authorities.
        <br/>
        <p><font color="red"><b>Notice:</b> Note that the information you are
        providing below may be leaked to anyone who can read the network traffic between
        you and this web server or who has access to this web server. If you need to
        keep the IP addresses and incident times confidential, you should download the
        <a href="https://svn.torproject.org/svn/projects/archives/trunk/exonerator/">Java
        or Python version of ExoneraTor</a> and run it on your local machine.</font></p>
        <br/>
        <a id="relay"/><h3>Was there a Tor relay running on this IP address?</h3>
        <form action="exonerator.html#relay">
          <input type="hidden" name="targetaddr" />
          <input type="hidden" name="targetPort"/>
          <table>
            <tr>
              <td align="right">IP address in question:</td>
              <td><input type="text" name="ip""/></td>
              <td><i>(Ex.: 1.2.3.4)</i></td>
            </tr>
            <tr>
              <td align="right">Timestamp, in UTC:</td>
              <td><input type="text" name="timestamp""/></td>
              <td><i>(Ex.: 2010-01-01 12:00)</i></td>
            </tr>
            <tr>
              <td/>
              <td>
                <input type="submit">
                <input type="reset">
              </td>
              <td/>
            </tr>
          </table>
        </form>
        <br/>
