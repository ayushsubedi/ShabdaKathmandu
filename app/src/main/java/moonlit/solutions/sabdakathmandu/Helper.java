package moonlit.solutions.sabdakathmandu;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import com.mapbox.geojson.Point;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.geojson.Polygon;
import com.mapbox.services.commons.models.Position;


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
    // subtracting 1 to avoid inconsistency in means caused by repeting points in enclosed polyline
    protected static LatLng houseCentroid(List<Point> pointList){
        double centroidLat = 0, centroidLon = 0;

        for (int i=0; i<pointList.size()-1; i++){
            centroidLat += pointList.get(i).latitude();
            centroidLon += pointList.get(i).longitude();
        }

        return new LatLng(centroidLat/(pointList.size()-1), centroidLon/(pointList.size()-1));
    }

    // TODO: Find the polygon pole of inaccessibility, not to be confused with centroid
    // TODO: this point, unlike centroid will always lie inside the polygon
    protected static LatLng housePoleOfInaccessibility(List<Point> pointList){
        // The input param is a list of Point (com.mapbox.geojson.Point)

        List<List<Position>> rings = new ArrayList<>();
        List<Position> outer_ring = new ArrayList<>();
        for (int i=0; i<pointList.size()-1; i++){
            outer_ring.add(Position.fromCoordinates(pointList.get(i).latitude(), pointList.get(i).longitude()));
        }
        rings.add(outer_ring);
        Polygon polygon = Polygon.fromCoordinates(rings);// Get polygon data from somewhere.
        com.mapbox.services.commons.geojson.Point p = Polylabel.polylabel(polygon, 100);
        LatLng output = new LatLng(p.getCoordinates().getLongitude(), p.getCoordinates().getLatitude());
        return output;
    }

    private static List<Position> test(List<Point> pointList){
        List<Position> coordinates = new ArrayList<>();
        for (int i=0; i<pointList.size()-1; i++){
            coordinates.add(Position.fromCoordinates(pointList.get(i).latitude(), pointList.get(i).longitude()));
        }
        return coordinates;
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
