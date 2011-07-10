package menion.android.locus.addon.publiclib.geoData;

import android.os.Parcel;
import android.os.Parcelable;

public class PointGeocachingDataWaypoint implements Parcelable {
	
	private static final int VERSION = 0;
	
	/* name of waypoint */
	public String name;
	/* description (may be HTML code) */
	public String description;
	/* type of waypoint (defined in PointGeocachingData) */
	public String type;
	/* image URL to this wpt */
	public String typeImagePath;
	/* latitude of waypoint */
	public double lat;
	/* longitude of waypoint */
	public double lon;
	
	public PointGeocachingDataWaypoint() {
		name = "";
		description = "";
		type = "";
		typeImagePath = "";
		lat = 0.0;
		lon = 0.0;
	}

	/****************************/
	/*      PARCELABLE PART     */
	/****************************/
	
    public static final Parcelable.Creator<PointGeocachingDataWaypoint> CREATOR = new Parcelable.Creator<PointGeocachingDataWaypoint>() {
        public PointGeocachingDataWaypoint createFromParcel(Parcel in) {
            return new PointGeocachingDataWaypoint(in);
        }

        public PointGeocachingDataWaypoint[] newArray(int size) {
            return new PointGeocachingDataWaypoint[size];
        }
    };
    
    public PointGeocachingDataWaypoint(Parcel in) {
    	switch (in.readInt()) {
    	case 0:
    		name = in.readString();
    		description = in.readString();
    		type = in.readString();
    		typeImagePath = in.readString();
    		lat = in.readDouble();
    		lon = in.readDouble();
    		break;
    	}
    }
    
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(VERSION);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(type);
		dest.writeString(typeImagePath);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
	}
	
	public int describeContents() {
		return 0;
	}
}