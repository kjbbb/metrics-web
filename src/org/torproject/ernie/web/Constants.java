package org.torproject.ernie.web;

import java.sql.*;
import java.text.*;
import java.util.TimeZone;

public class Constants {

    public final String jdbcURL = "jdbc:postgresql://localhost/tordir";

    public final String jdbcUser = "ernie";

    public final String jdbcPassword = "";

    public Connection conn;

    public SimpleDateFormat simpledf;

    public Constants()  {

        this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
        this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
        } catch (SQLException e) {
        } catch (ClassNotFoundException e)  {
            System.out.println("Class not found");
        }
    }
}

