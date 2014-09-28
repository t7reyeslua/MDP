package tudelft.mdp.ui;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.CardExpand;
import tudelft.mdp.R;

public class DeviceCardExpand extends CardExpand {

    private String timeTotal;
    private String timeYou;
    private String powerTotal;
    private String powerYou;
    private String usagePercentage;
    //Use your resource ID for your inner layout
    public DeviceCardExpand(Context context,
            String timeTotal, String timeYou,
            String powerTotal, String powerYou,
            String usagePercentage) {

        super(context, R.layout.card_device_custom_expand);
        this.timeTotal = timeTotal;
        this.timeYou = timeYou;
        this.powerTotal = powerTotal;
        this.powerYou = powerYou;
        this.usagePercentage = usagePercentage;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view == null) return;

        //Retrieve TextView elements
        TextView tx1 = (TextView) view.findViewById(R.id.twTotalTime);
        TextView tx2 = (TextView) view.findViewById(R.id.twYouTime);
        TextView tx3 = (TextView) view.findViewById(R.id.twTotalPower);
        TextView tx4 = (TextView) view.findViewById(R.id.twYouPower);
        TextView tx5 = (TextView) view.findViewById(R.id.twUsagePercentage);

        //Set value in text views
        if (tx1 != null) {
            tx1.setText(timeTotal);
        }

        if (tx2 != null) {
            tx2.setText(timeYou);
        }

        if (tx3 != null) {
            tx3.setText(powerTotal);
        }

        if (tx4 != null) {
            tx4.setText(powerYou);
        }

        if (tx5 != null) {
            tx5.setText(usagePercentage);
        }

    }
}