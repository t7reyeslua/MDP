package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class EnergyConsumptionRecord {

    @Id
    private Long id;
    @Index
    private String username;
    @Index
    private Double userEnergy;
    @Index
    private Double groupEnergy;
    @Index
    private String timestamp;

    public EnergyConsumptionRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getUserEnergy() {
        return userEnergy;
    }

    public void setUserEnergy(Double userEnergy) {
        this.userEnergy = userEnergy;
    }

    public Double getGroupEnergy() {
        return groupEnergy;
    }

    public void setGroupEnergy(Double groupEnergy) {
        this.groupEnergy = groupEnergy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
