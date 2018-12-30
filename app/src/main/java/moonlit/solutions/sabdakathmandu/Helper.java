package moonlit.solutions.sabdakathmandu;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Helper {


    // Todo: change this to convex hull
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
}
