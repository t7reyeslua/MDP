package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;

import java.util.ArrayList;

@Entity
public class ApHistogramRecordWrapper {


    private ArrayList<ApHistogramRecord> localHistogram = new ArrayList<ApHistogramRecord>();

    public ApHistogramRecordWrapper() {
    }

    public ArrayList<ApHistogramRecord> getLocalHistogram() {
        return localHistogram;
    }

    public void setLocalHistogram(ArrayList<ApHistogramRecord> localHistogram) {
        this.localHistogram = localHistogram;
    }
}
