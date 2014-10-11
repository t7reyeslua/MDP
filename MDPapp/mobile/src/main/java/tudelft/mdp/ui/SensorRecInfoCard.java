package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;


public class SensorRecInfoCard extends Card {

    private Context context;

    protected TextView twSensorType;
    protected TextView twValue;

    private String sensorType;
    private String values;

    public SensorRecInfoCard(Context context, String sensorType, String values) {
        super(context, R.layout.card_sensor_rec_info);
        this.context = context;
        this.sensorType = sensorType;
        this.values = values;
    }

    public SensorRecInfoCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        twSensorType = (TextView) parent.findViewById(R.id.twSensorType);
        twValue  = (TextView) parent.findViewById(R.id.twValues);

        if (twSensorType != null){
            twSensorType.setText(sensorType);
        }
        if (twValue != null){
            twValue.setText(values);
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

    public TextView getTwSensorType() {
        return twSensorType;
    }

    public void setTwSensorType(TextView twSensorType) {
        this.twSensorType = twSensorType;
    }

    public TextView getTwValue() {
        return twValue;
    }

    public void setTwValue(TextView twValue) {
        this.twValue = twValue;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
