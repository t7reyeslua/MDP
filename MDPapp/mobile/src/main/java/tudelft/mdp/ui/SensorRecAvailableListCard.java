package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;


public class SensorRecAvailableListCard extends Card {

    private Context context;


    public SensorRecAvailableListCard(Context context) {
        super(context, R.layout.card_sensor_rec_list);
        this.context = context;
    }

    public SensorRecAvailableListCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements

    }

}
