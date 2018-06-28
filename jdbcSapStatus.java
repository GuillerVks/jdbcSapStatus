import java.sql.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class jdbcSapStatus{
    public static void main(String[] argv) {

        Connection connection = null;
        BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader("hosts_hana.txt");
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {

                if (sCurrentLine.startsWith("#") || sCurrentLine.length() == 0) continue;

                String[] params = sCurrentLine.split(";");
                if(sCurrentLine.length() > 0 && params.length == 0 || params.length < 2)
                {
                    System.err.println("ERROR: Params required missing\n => FORMAT IN FILE: Hostname:port;service;description");
                } else {
                    String usr = "";
                    String pwd = "";
                    String host = params[0];
                    String service = params[1];
                    String description = "";
                    if (params.length > 2 && params[2] != null) { description = params[2]; }

                    if (host==null || service==null) {
                        System.err.println("ERROR: Params required missing");
                    } else{
                        try{
                            String connectionString = "jdbc:sap://"+host+"/?autocommit=false";
                            connection = DriverManager.getConnection(connectionString, usr, pwd);

                            if (connection != null) {
                                try {
                                    String msg = "Connection to HANA successful!";
                                    Statement stmt = connection.createStatement();
                                    ResultSet resultSet = stmt.executeQuery("Select 'hello world' from dummy");
                                    resultSet.next();

                                    callMonitor(host, msg, "OK", service, description);

                                } catch (SQLException e) {
                                    callMonitor(host, "Query failed!", "NOK", service, description);
                                }
                            } else {
                                callMonitor(host, "Sin conexión SAP!", "NOK", service, description);
                            }
                        } catch (SQLException e) {
                            callMonitor(host, e.getMessage(), "NOK", service, description);
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

    public static void callMonitor(String host, String message, String status, String service, String description){
        try {
            String url_monitor = "http://url.elastic";
            URL obj = new URL(url_monitor);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            String data = "{\"primary_class\":\"SAP\",\"secondary_class\":\"HANA\",\"host\":\""+ host +"\",\"message\":\""+message+"\",\"status\":\""+status+"\",\"service\":\""+service+"\",\"description\":\""+description+"\"}";

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data);
            out.close();

            new InputStreamReader(conn.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}