package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.ToggleButton;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;


public class LocatorNewScanCard extends Card {

    private Context context;

    protected Button mButton;
    protected ProgressBar mProgressBar;

    public LocatorNewScanCard(Context context) {
        super(context, R.layout.card_locator_newscan);
        this.context = context;
    }

    public LocatorNewScanCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mButton  = (Button) parent.findViewById(R.id.btnNewScan);
        mProgressBar   = (ProgressBar) parent.findViewById(R.id.progressBar);

    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }


    public Button getButton() {
        return mButton;
    }

    public void setButton(Button button) {
        mButton = button;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }
}
