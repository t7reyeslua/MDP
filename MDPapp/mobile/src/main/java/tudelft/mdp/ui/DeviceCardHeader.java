package tudelft.mdp.ui;

import com.devspark.robototextview.widget.RobotoTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

public class DeviceCardHeader extends CardHeader {

    private String header;
    private String subtitle;

    public DeviceCardHeader(Context context, String header, String subtitle) {
        super(context, R.layout.card_device_custom_header);
        this.header = header;
        this.subtitle = subtitle;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view!=null){
            TextView t1 = (RobotoTextView) view.findViewById(R.id.text_header);
            if (t1!=null)
                t1.setText(header);

            TextView t2 = (RobotoTextView) view.findViewById(R.id.text_subtitle);
            if (t2!=null)
                t2.setText(subtitle);
        }
    }
}
