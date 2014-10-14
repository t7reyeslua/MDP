package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.gmariotti.cardslib.library.internal.CardExpand;
import tudelft.mdp.R;

public class CalibrationNetworksExpand extends CardExpand {

    private Button mButton;

    public CalibrationNetworksExpand(Context context) {

        super(context, R.layout.card_calibration_networks);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        mButton = (Button) view.findViewById(R.id.buttonRegression);


    }
}
