import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.sql.*;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;

public class SensorInfo {
    //Constructor
    public SensorInfo(String jqueryString){
        this.jqueryString = jqueryString;

        this.allocateSensorInfo(jqueryString);
    }

    //Variables
    private String jqueryString;
    private byte[] physicalAddress = new byte[6]; //Mac Address
    private JSONObject dataObject;

    //Getters & Setters

    //Functions
    public void submitSensorData(){
        System.out.println("Submitting sensor data.....");

        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : this.physicalAddress){
            stringBuilder.append(String.format("%02x", b));
        }
        System.out.println(stringBuilder.toString());

        Connection connection = null;
        PreparedStatement preparedStatement;
        String database = "UrbanFrontierHouse";
        String url = ("jdbc:mysql://mysql.scritch.ninja:3306/" + database + "?serverTimezone=UTC");
        String user = "Urban Frontier User";
        String password = "urbanfrontieruser2016";
        String query = "INSERT INTO UrbanFrontierHouse.SensorLog (sensorId, value, type) VALUES (unhex(?), ?, ?)";

        try{
            connection = DriverManager.getConnection(url, user, password);

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
        Iterator iterator = dataObject.keySet().iterator();

        int n = 0;
        while(iterator.hasNext()) {
            Object object = iterator.next();
            try {
                preparedStatement.setString(1, stringBuilder.toString());
                preparedStatement.setInt(2, Integer.parseInt(dataObject.get(object.toString()).toString()));
                preparedStatement.setString(3, object.toString());
                preparedStatement.executeUpdate();
                System.out.println("....Sensor data submitted");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            n++;
        }
    }

    private void allocateSensorInfo(String jqueryToAllocate){
        StringBuilder newJquery = new StringBuilder(jqueryToAllocate);

        if(!jqueryToAllocate.contains(",\"data") && !jqueryToAllocate.contains(", \"data")){
            int replaceCommaIndex = jqueryToAllocate.indexOf("\"data");
            newJquery.setCharAt(replaceCommaIndex - 1, ',');
        }

        jqueryToAllocate = newJquery.toString();

        //Run it through Json Object
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject)parser.parse(jqueryToAllocate);
            this.dataObject = (JSONObject)parser.parse(jsonObject.get("data").toString());

            System.out.println(Arrays.toString(parsePhysicalAddress(jsonObject.get("mac").toString())));
            physicalAddress = parsePhysicalAddress(jsonObject.get("mac").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private byte[] parsePhysicalAddress(String macAddressString){
        byte[] macAddress = new byte[6];
        String[] bytes = macAddressString.split(":", 6);

        for (int x = 0; x < bytes.length; x++) {
            macAddress[x] = Integer.valueOf(bytes[x], 16).byteValue();
        }

        return macAddress;
    }

    //        String parseString = "{\"mac\": \"AA:AA:AA:BB:AA:AA\",\"data\": {\"temperature\": 222, \"humidity\": 333}}";
////        JSONParser parser = new JSONParser();
////        try {
////            JSONObject jsonObject = (JSONObject)parser.parse(parseString);
////            JSONObject dataObject = (JSONObject)parser.parse(jsonObject.get("data").toString());
////            Iterator iterator = dataObject.keySet().iterator();
////
////            while(iterator.hasNext()) {
////                Object object = iterator.next();
////                System.out.println(object.toString() + ": " + dataObject.get(object.toString()));
////            }
////        } catch (ParseException e) {
////            e.printStackTrace();
////        }
}
