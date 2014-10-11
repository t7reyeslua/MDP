package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;


public class SensorRecControlCard extends Card {

    private Context context;

    protected AutoCompleteTextView acFile;
    protected ToggleButton mToggleButton;
    protected ProgressBar mProgressBar;
    protected Chronometer mChronometer;

    public SensorRecControlCard(Context context) {
        super(context, R.layout.card_sensor_rec_control);
        this.context = context;
    }

    public SensorRecControlCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        acFile        = (AutoCompleteTextView) parent.findViewById(R.id.acFile);
        mToggleButton  = (ToggleButton) parent.findViewById(R.id.toggleButton);
        mProgressBar      = (ProgressBar) parent.findViewById(R.id.progressBar);
        mChronometer = (Chronometer) parent.findViewById(R.id.chronometer);

    }


    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public AutoCompleteTextView getAcPlace() {
        return acFile;
    }

    public void setAcPlace(AutoCompleteTextView acPlace) {
        this.acFile = acPlace;
    }


    public ToggleButton getToggleButton() {
        return mToggleButton;
    }

    public void setToggleButton(ToggleButton toggleButton) {
        mToggleButton = toggleButton;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }
}
