package menion.android.locus.addon.publiclib.geoData;

import android.os.Parcel;
import android.os.Parcelable;

public class PointGeocachingDataLog  implements Parcelable {
	
	private static final int VERSION = 0;
	
	public int type;
	public String date;
	public String finder;
	public int finderFound;
	public String logText;

	public PointGeocachingDataLog() {
		type = PointGeocachingData.CACHE_LOG_TYPE_UNKNOWN;
		date = "";
		finder = "";
		finderFound = 0;
		logText = "";
	}
	
	/****************************/
	/*      PARCELABLE PART     */
	/****************************/
	
    public static final Parcelable.Creator<PointGeocachingDataLog> CREATOR = new Parcelable.Creator<PointGeocachingDataLog>() {
        public PointGeocachingDataLog createFromParcel(Parcel in) {
            return new PointGeocachingDataLog(in);
        }

        public PointGeocachingDataLog[] newArray(int size) {
            return new PointGeocachingDataLog[size];
        }
    };
    
    public PointGeocachingDataLog(Parcel in) {
    	switch (in.readInt()) {
    	case 0:
    		type = in.readInt();
    		date = in.readString();
    		finder = in.readString();
    		finderFound = in.readInt();
    		logText = in.readString();
    		break;
    	}
    }
    
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(VERSION);
		dest.writeInt(type);
		dest.writeString(date);
		dest.writeString(finder);
		dest.writeInt(finderFound);
		dest.writeString(logText);
	}
	
	public int describeContents() {
		return 0;
	}
}