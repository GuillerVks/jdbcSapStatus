import java.sql.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.logging.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress;

public class jdbcSapStatus{
    public static void main(String[] argv) {

        // Class.forName("com.sap.db.jdbc.Driver"); //THIS IS ONLY FOR JAVA 6 OR LESS.
        Connection connection = null;
        BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader("hosts_hana.txt");
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] params = sCurrentLine.split(" ");
                if(params.length == 0 || params.length < 3)
                {
                    System.err.println("ERROR: Params required missing\n => FORMAT IN FILE: Hostname:port user password");
                } else {
                    String host = params[0];
                    String usr = params[1];
                    String pwd = params[2];
                    if (host==null || usr==null || pwd==null) {
                        System.err.println("ERROR: Params required missing");
                    } else{
                        try{
                            String connectionString = "jdbc:sap://"+host+"/?autocommit=false";
                            connection = DriverManager.getConnection(connectionString, usr, pwd);
                        } catch (SQLException e) {
                            callMonitor(host, e.getMessage(), "NOK");
                        }
                        if (connection != null) {
                            try {
                                String msg = "Connection to HANA successful!";
                                Statement stmt = connection.createStatement();
                                ResultSet resultSet = stmt.executeQuery("Select 'hello world' from dummy");
                                resultSet.next();

                                callMonitor(host, msg, "OK");

                            } catch (SQLException e) {
                                callMonitor(host, "Query failed!", "NOK");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void callMonitor(String host, String message, String status){
        try {
            String url_monitor = "http://elastic.url";
            URL obj = new URL(url_monitor);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            String data = "{\"primary_class\":\"SAP\",\"secondary_class\":\"HANA\",\"host\":\""+ host +"\",\"message\":\""+message+"\",\"status\":\""+status+"\"}";

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data);
            out.close();

            new InputStreamReader(conn.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}