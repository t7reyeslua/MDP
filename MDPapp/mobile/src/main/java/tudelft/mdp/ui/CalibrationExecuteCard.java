package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;

public class CalibrationExecuteCard extends Card {

    public CalibrationExecuteCard(Context context) {
        super(context, R.layout.card_calibration_execute_calibrate);
    }

    public CalibrationExecuteCard(Context context, int innerLayout) {
        super(context, innerLayout);
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements

    }
}
