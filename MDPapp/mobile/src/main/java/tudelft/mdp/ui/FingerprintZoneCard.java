package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;

/**
 * Created by t7 on 7-10-14.
 */
public class FingerprintZoneCard extends Card {

    private Context context;

    protected TextView twPlace;
    protected TextView twZone;
    protected TextView twSamples;

    private String zone;
    private String place;
    private Integer samples;

    public FingerprintZoneCard(Context context, String place, String zone, Integer samples) {
        super(context, R.layout.card_zone_fingerprint_info);
        this.context = context;
        this.place = place;
        this.zone = zone;
        this.samples = samples;
    }

    public FingerprintZoneCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        twPlace = (TextView) parent.findViewById(R.id.twPlace);
        twZone  = (TextView) parent.findViewById(R.id.twZone);
        twSamples  = (TextView) parent.findViewById(R.id.twSamples);

        if (twPlace != null){
            twPlace.setText(place);
        }
        if (twZone != null){
            twZone.setText(zone);
        }
        if (twSamples != null){
            twSamples.setText(samples.toString());
        }



    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public TextView getTwPlace() {
        return twPlace;
    }

    public void setTwPlace(TextView twPlace) {
        this.twPlace = twPlace;
    }

    public TextView getTwZone() {
        return twZone;
    }

    public void setTwZone(TextView twZone) {
        this.twZone = twZone;
    }

    public TextView getTwSamples() {
        return twSamples;
    }

    public void setTwSamples(TextView twSamples) {
        this.twSamples = twSamples;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getSamples() {
        return samples;
    }

    public void setSamples(Integer samples) {
        this.samples = samples;
    }
}
