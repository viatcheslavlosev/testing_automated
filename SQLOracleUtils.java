package lib;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author VOLosev
 * данный класс содержит методы позволяющие работать с БД через JDBC
 */

public class SQLOracleUtils {


    /**
     *
     * метод возвращающий объект класса Connection
     */

    public static Connection connect (String connStr, String connUser, String connPass) {

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();

        }



        //System.out.println("Oracle JDBC Driver Registered!");

        Connection conn = null;

        try {


            conn = DriverManager.getConnection(connStr, connUser, connPass);

            if (conn != null) {
                //System.out.println(" - Your connection alive!");
            } else {
                System.out.println("Failed to make connection!");
            }


        } catch (SQLException e) {
            e.printStackTrace();

        }

        return conn;
    }

    // Simple connection
    public static Connection connect () {

        StartParameters.getStartParameters();
        String connStr = StartParameters.connStr;
        String connUser = StartParameters.connUser;
        String connPass = StartParameters.connPass;

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();

        }



        //System.out.println("Oracle JDBC Driver Registered!");

        Connection conn = null;

        try {


            conn = DriverManager.getConnection(connStr, connUser, connPass);

            if (conn != null) {
                //System.out.println(" - Your connection alive!");
            } else {
                System.out.println("Failed to make connection!");
            }


        } catch (SQLException e) {
            e.printStackTrace();

        }

        return conn;
    }

    /**
     *
     * метод позволяющий модифицировать данные и получающий в качестве параметра объект класса Connection и строку запроса
     */

        public static void changeDataDB (String strUpd, Connection conn) {


        try {


            PreparedStatement prep = conn.prepareStatement(strUpd);
            prep.execute();
            conn.commit();


        } catch (SQLException e) {

            System.out.println("Update is Failed! Check output console");
            e.printStackTrace();
            return;

        }


    }


    /**
     *
     * метод позволяющий выбирать единственную строку с данными и получающий в качестве параметра объект класса Connection и строку запроса
     */

    public static String selectStrValueDB (String selectStr, Connection conn) {

        String selectStrS = "";

        try {
            //String selectStrS = "";
            PreparedStatement prep = conn.prepareStatement(selectStr);
            //prep.
            prep.execute();

            ResultSet rs = prep.getResultSet();

            while(rs.next()) {

                selectStrS = rs.getString(1);

            }

            if (selectStr != null) {
                //System.out.println("selectStr --  " + selectStr);

            } else {
                return "-- selectStr не получен -- ";
            }


        } catch (SQLException e) {

            e.printStackTrace();

        }

        return selectStrS;

    }

    /**
     *
     *
     */

    public static ArrayList selectStrValueDBArray (String selectStr, Connection conn) {

        ArrayList selectStrS = new ArrayList<String>();
        ResultSet rs = null;

        try {

            PreparedStatement prep = conn.prepareStatement(selectStr);
            prep.execute();

            rs = prep.getResultSet();
            String resultSTRlist;

            while(rs.next()) {

                resultSTRlist = rs.getString(1);
                selectStrS.add(resultSTRlist);

            }


        } catch (SQLException e) {

            e.printStackTrace();
        }

        return selectStrS;

    }

    /**
     *
     *
     */

    public static int[] DBArrayLength (String selectStr, Connection conn)   {

        ResultSet rs = null;
        int[] sizeSQLResultset = new int[2];

        try {

            PreparedStatement prep = null;
            prep = conn.prepareStatement(selectStr);
            prep.execute();

            int amountColumn = prep.getMetaData().getColumnCount();
            rs = prep.getResultSet();

            int amountRows=0;
            while (rs.next()) {
                amountRows++;
            }

            sizeSQLResultset[0] = amountColumn;
            sizeSQLResultset[1] = amountRows;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sizeSQLResultset;
    }

    /**
     *
     *
     */

    public static  String[][] selectStrValueDBArray (String selectStr, int[] sizeSQLResultset, Connection conn) {

        ResultSet rs = null;
        int amountColumn = sizeSQLResultset[0];
        int amountRows = sizeSQLResultset[1];
        String[][] ResultValue = new String[amountRows][amountColumn];
        PreparedStatement prep = null;

        try {
            prep = conn.prepareStatement(selectStr);
            prep.execute();
            rs = prep.getResultSet();

            for (int j = 0; j < amountRows; j++) {
                rs.next();
                for (int i = 0; i < amountColumn; i++) {

                    ResultValue[j][i] = rs.getString(i+1);

                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ResultValue;
    }

    /**
     *
     *
     */

    public static  ArrayList selectStrValueDBArrayList (String selectStr, Connection conn) {

        ResultSet rs = null;
        ArrayList arrSlct = new ArrayList<String[]>();
        PreparedStatement prep = null;

        try {
            prep = conn.prepareStatement(selectStr);
            prep.execute();
            int amountColumnSlct = prep.getMetaData().getColumnCount();
            rs = prep.getResultSet();

            while (rs.next()) {
                String[] resultSTRlist = new String[amountColumnSlct];

                for (int i = 0; i < amountColumnSlct; i++) {
                    resultSTRlist[i] = rs.getString(i+1);

                }
                arrSlct.add(resultSTRlist);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return arrSlct;
    }



}
