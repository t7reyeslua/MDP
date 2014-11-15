package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;

import java.util.ArrayList;

@Entity
public class LocationFeaturesRecordWrapper {

    ArrayList<LocationFeaturesRecord> mLocationFeaturesRecords = new ArrayList<LocationFeaturesRecord>();

    public LocationFeaturesRecordWrapper() {
    }

    public ArrayList<LocationFeaturesRecord> getLocationFeaturesRecords() {
        return mLocationFeaturesRecords;
    }

    public void setLocationFeaturesRecords(
            ArrayList<LocationFeaturesRecord> locationFeaturesRecords) {
        mLocationFeaturesRecords = locationFeaturesRecords;
    }
}
