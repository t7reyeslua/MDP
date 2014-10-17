package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import java.util.ArrayList;

@Entity
public class LocationFingerprintRecordWrapper {


    private ArrayList<LocationFingerprintRecord> mLocationFingerprintRecordWrapperArrayList
            = new ArrayList<LocationFingerprintRecord>();

    public LocationFingerprintRecordWrapper() {
    }

    public ArrayList<LocationFingerprintRecord> getLocationFingerprintRecordWrapperArrayList() {
        return mLocationFingerprintRecordWrapperArrayList;
    }

    public void setLocationFingerprintRecordWrapperArrayList(
            ArrayList<LocationFingerprintRecord> locationFingerprintRecordWrapperArrayList) {
        mLocationFingerprintRecordWrapperArrayList = locationFingerprintRecordWrapperArrayList;
    }
}
