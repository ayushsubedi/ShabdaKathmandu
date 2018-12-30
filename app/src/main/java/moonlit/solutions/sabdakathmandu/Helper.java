package moonlit.solutions.sabdakathmandu;

import android.content.Context;
import android.content.res.AssetManager;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Helper {
    private static List<String> words = new ArrayList<>();

    protected static LatLng houseCenter(List<Point> pointList){
        ArrayList<Double> lat_list = new ArrayList<>();
        ArrayList<Double> lon_list = new ArrayList<>();
        for (int i = 0; i< pointList.size(); i++){
            lat_list.add(pointList.get(i).latitude());
            lon_list.add(pointList.get(i).longitude());
        }
        double centerLatitude = ( Collections.min(lat_list) + Collections.max(lat_list) ) / 2;
        double centerLongitude = ( Collections.min(lon_list) + Collections.max(lon_list) ) / 2;
        return new LatLng(centerLatitude, centerLongitude);
    }


    // TODO this is still not the best solution, there are going to be cases where centroid will lie
    // outside the polygon

    protected static LatLng houseCentroid(List<Point> pointList){
        double centroidLat = 0, centroidLon = 0;

        for (int i=0; i<pointList.size(); i++){
            centroidLat += pointList.get(i).latitude();
            centroidLon += pointList.get(i).longitude();
        }

        return new LatLng(centroidLat/pointList.size(), centroidLon/pointList.size());
    }

    private static List<String> getWords(Context context){
        List<String> words = new ArrayList<>();
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open("android.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    protected static String encodeLocationToWords(Context context, LatLng target){
        if (words == null || words.size()== 0){
            words = getWords(context);
        }
        double lat = target.getLatitude();
        double lon = target.getLongitude();
        int int_lat = ((int) (lat*10000)) -  270000 - 5000;
        int int_lon = ((int) (lon*10000)) -  850000 - 2000;
        try {
            return(String.format("%s.%s", words.get(int_lat), words.get(int_lon)));
        }
        catch (IndexOutOfBoundsException e){
            return ("----.----");
        }
    }
}
