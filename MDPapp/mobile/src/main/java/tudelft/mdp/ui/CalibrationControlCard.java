package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.ToggleButton;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;


public class CalibrationControlCard extends Card {

    private Context context;

    protected ToggleButton mToggleButton;
    protected ProgressBar mProgressBar;
    protected Switch mSwitch;

    public CalibrationControlCard(Context context) {
        super(context, R.layout.card_calibration_control);
        this.context = context;
    }

    public CalibrationControlCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mToggleButton  = (ToggleButton) parent.findViewById(R.id.toggleButton);
        mProgressBar   = (ProgressBar) parent.findViewById(R.id.progressBar);
        mSwitch        = (Switch) parent.findViewById(R.id.swMaster);

    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public Switch getSwitch() {
        return mSwitch;
    }

    public void setSwitch(Switch aSwitch) {
        mSwitch = aSwitch;
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
