package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/** The Objectify object model for "unique" nfc tags  we are persisting */
@Entity
public class NfcRecord {

    @Id
    private String nfcId;

    @Index
    private String type;
    @Index
    private String description;
    @Index
    private Integer state;
    @Index
    private String Location;
    @Index
    private String Place;



    public NfcRecord() {
    }

    public NfcRecord(String nfcId, Integer state, String description) {
        this.nfcId = nfcId;
        this.state = state;
        this.description = description;
    }

    public String getNfcId() {
        return nfcId;
    }

    public void setNfcId(String nfcId) {
        this.nfcId = nfcId;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getPlace() {
        return Place;
    }

    public void setPlace(String place) {
        Place = place;
    }
}
