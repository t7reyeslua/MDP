package tudelft.mdp.ui;

import com.devspark.robototextview.widget.RobotoTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

public class LocatorHeader extends CardHeader {

    private String header;

    public LocatorHeader(Context context, String header) {
        super(context, R.layout.card_locator_header);
        this.header = header;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view!=null){
            TextView t1 = (RobotoTextView) view.findViewById(R.id.text_header);
            if (t1!=null)
                t1.setText(header);
        }
    }
}
