
package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import tudelft.mdp.R;


public class CalibrationCurrentValuesCard extends Card {

    private Context context;

    protected TextView twPreviouslyCalibrated;
    protected TextView twValue;

    private String previouslyCalibrated;
    private String values;

    public CalibrationCurrentValuesCard(Context context, String previouslyCalibrated, String values) {
        super(context, R.layout.card_calibration_current_values);
        this.context = context;
        this.previouslyCalibrated = previouslyCalibrated;
        this.values = values;

        CardExpand expand = new CardExpand(context);
        this.addCardExpand(expand);
        init();
    }

    public CalibrationCurrentValuesCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
        init();
    }

    private void toggleExpand(){
        if (this.isExpanded()){
            this.doCollapse();
        } else {
            this.doExpand();
        }
    }

    private void init(){

        //No Header


        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                //Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
                //toggleExpand();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        twPreviouslyCalibrated = (TextView) parent.findViewById(R.id.twPreviousCalibration);
        twValue  = (TextView) parent.findViewById(R.id.twValues);

        if (twPreviouslyCalibrated != null){
            twPreviouslyCalibrated.setText(previouslyCalibrated);
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

    public TextView getTwPreviouslyCalibrated() {
        return twPreviouslyCalibrated;
    }

    public void setTwPreviouslyCalibrated(TextView twPreviouslyCalibrated) {
        this.twPreviouslyCalibrated = twPreviouslyCalibrated;
    }

    public TextView getTwValue() {
        return twValue;
    }

    public void setTwValue(TextView twValue) {
        this.twValue = twValue;
    }

    public String getPreviouslyCalibrated() {
        return previouslyCalibrated;
    }

    public void setPreviouslyCalibrated(String previouslyCalibrated) {
        this.previouslyCalibrated = previouslyCalibrated;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
