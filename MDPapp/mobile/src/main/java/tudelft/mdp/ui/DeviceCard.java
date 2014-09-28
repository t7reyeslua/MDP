package tudelft.mdp.ui;


import com.devspark.robototextview.widget.RobotoTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;

public class DeviceCard extends Card {
    protected TextView mSecondaryTitle;
    protected TextView mNFCtag;
    protected TextView mCurrentUsers;
    protected TextView mShadow;

    private String nfcTag;
    private String currentUsers;
    private boolean active;

    private Context context;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public DeviceCard(Context context) {
        this(context, R.layout.card_device_custom);
    }

    public DeviceCard(Context context,String nfcTag, String currentUsers, Boolean active) {
        this(context, R.layout.card_device_custom);
        this.nfcTag = nfcTag;
        this.currentUsers = currentUsers;
        this.context = context;
        this.active = active;
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public DeviceCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    /**
     * Init
     */
    private void init(){

        //No Header


        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                //Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements

        mNFCtag = (RobotoTextView) parent.findViewById(R.id.device_card_nfc_tag);
        mCurrentUsers = (RobotoTextView) parent.findViewById(R.id.device_card_current_users);
        mShadow = (TextView) parent.findViewById(R.id.device_card_shadow);

        if (mNFCtag!=null)
            mNFCtag.      setText("Device Tag: " + nfcTag);

        if (mCurrentUsers!=null)
            mCurrentUsers.setText("Current Users: " + currentUsers);

        if (mShadow!=null) {
            if (!active){
                mShadow.setBackgroundColor(context.getResources().getColor(R.color.SlateGray));
            }
        }


    }
}
