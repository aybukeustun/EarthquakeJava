import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    private static  String URL ="https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&";

    public static void main(String[] args)  {


        LocalDate endtime= LocalDate.now();
        Scanner input =new Scanner(System.in);
        System.out.print("Please enter the number of days you want search :");
        int end = input.nextInt();
        LocalDate date = endtime.plusDays(-end);
        boolean countryExist= false;
        boolean countryName =false;
        String countryUs= null;



        try {

            URL url = new URL(URL+"starttime="+date + "&endtime="+endtime);

            Scanner countryPicker =new Scanner(System.in);
            System.out.print("Please enter country you want search  :");
            String country = countryPicker.nextLine();
            JSONParser jsonParser = new JSONParser();
            try {
                Object objc = jsonParser.parse(new FileReader("country.json"));
                //Object objects =jsonParser.parse(new FileReader("states.json"));
                JSONArray countries = (JSONArray) objc;
                //JSONArray states = (JSONArray) objects;
                for (int i = 0; i < countries.size(); i++) {
                    JSONObject countryobj = (JSONObject) countries.get(i);
                    if(countryobj.get("name").equals(country)){
                        countryUs = country;
                        countryName =true;

                    } /*else {
                        for (int j = 0; j < states.size(); j++) {
                            JSONObject stateobj = (JSONObject) states.get(j);
                            if(stateobj.get("name").equals(country)){
                                countryUs= "United States of America";
                                countryName =true;
                            }
                    }

                }*/
            }

            }catch (ParseException e) {
                throw new RuntimeException(e);
            }


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responsecode = conn.getResponseCode();

            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {

                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }

                scanner.close();

                JsonElement data_obj = JsonParser.parseString(inline);

                JsonObject obj = data_obj.getAsJsonObject();
                JsonArray featuresArray = obj.getAsJsonArray("features");
                if(countryName){
                for (int i = 0; i < featuresArray.size(); i++) {
                    JsonObject object = featuresArray.get(i).getAsJsonObject().get("properties").getAsJsonObject();

                    if (object.get("place").toString().contains(country)){
                        countryExist =true;
                        String timestamp = object.get("time").toString();
                        Date date1 =new Date(Long.parseLong(timestamp));
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String myDate = format.format(date1);
                        String place= object.get("place").toString();
                        String mag =object.get("mag").toString();
                        System.out.println("Country: "+countryUs+", Place of the earthquake :"+place +", Magnitude: "+mag +" , Date and time of earthquake: "+myDate);
                    }else if(i==featuresArray.size()-1 &&countryExist==false){
                        Logger logger
                                = Logger.getLogger(
                                Main.class.getName());

                        logger.setLevel(Level.WARNING);

                        logger.warning("No Earthquakes were recorded past "+end+ " days in "+countryUs);
                       // System.out.println("Warning: No Earthquakes were recorded past "+end+ " days in "+country);
                    }

                }
                }
                else{
                    System.out.println("This Country name is invalid!");
                }
            }
        }catch (IOException e){

        }
    }
}