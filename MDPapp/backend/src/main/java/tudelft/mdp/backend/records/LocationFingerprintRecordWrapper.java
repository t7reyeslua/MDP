package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

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

    public void addLocationFingerprintRecord(LocationFingerprintRecord locationFingerprintRecord){
        mLocationFingerprintRecordWrapperArrayList.add(locationFingerprintRecord);
    }

    public void clearLocationFingerprintRecordArrayList(){
        mLocationFingerprintRecordWrapperArrayList.clear();
    }
}
