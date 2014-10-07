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

/**
 * Created by t7 on 7-10-14.
 */
public class FingerprintControlCard extends Card {

    private Context context;

    protected AutoCompleteTextView acPlace;
    protected AutoCompleteTextView acZone;
    protected ToggleButton mToggleButton;
    protected ProgressBar mProgressBar;
    protected Chronometer mChronometer;

    public FingerprintControlCard(Context context) {
        super(context, R.layout.card_fingerprint_control);
        this.context = context;
    }

    public FingerprintControlCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        acPlace        = (AutoCompleteTextView) parent.findViewById(R.id.acPlace);
        acZone  = (AutoCompleteTextView) parent.findViewById(R.id.acZone);
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
        return acPlace;
    }

    public void setAcPlace(AutoCompleteTextView acPlace) {
        this.acPlace = acPlace;
    }

    public AutoCompleteTextView getAcZone() {
        return acZone;
    }

    public void setAcZone(AutoCompleteTextView acZone) {
        this.acZone = acZone;
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
