package tudelft.mdp.ui;


import com.devspark.robototextview.widget.RobotoTextView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApGaussianRecord;
import tudelft.mdp.locationTracker.NetworkInfoObject;

public class ExpandableListAdapterRSSI extends BaseExpandableListAdapter {

    public ArrayList<ApGaussianRecord> tempChild;
    public ArrayList<NetworkInfoObject> groupItem = new ArrayList<NetworkInfoObject>();
    public ArrayList<Object> Childtem = new ArrayList<Object>();
    public LayoutInflater minflater;
    public Activity activity;
    private final Context context;

    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };

    public ExpandableListAdapterRSSI(Context context, ArrayList<NetworkInfoObject> grList,
            ArrayList<Object> childItem) {
        this.context = context;
        groupItem = grList;
        this.Childtem = childItem;
    }

    public void setInflater(LayoutInflater mInflater, Activity act) {
        this.minflater = mInflater;
        activity = act;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        tempChild = (ArrayList<ApGaussianRecord>) Childtem.get(groupPosition);
        TextView text = null;
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_subitem_scanned_aps,parent,false);
        }

        RobotoTextView twLoc = (RobotoTextView) convertView.findViewById(R.id.textLocation);
        RobotoTextView twZone = (RobotoTextView) convertView.findViewById(R.id.textSection);
        RobotoTextView twStd = (RobotoTextView) convertView.findViewById(R.id.textStd);
        RobotoTextView twMean = (RobotoTextView) convertView.findViewById(R.id.textMean);

        twLoc.setText(tempChild.get(childPosition).getPlace());
        twZone.setText(tempChild.get(childPosition).getZone());
        twStd.setText(String.format("%1$,.3f", tempChild.get(childPosition).getStd()));
        twMean.setText(String.format("%1$,.3f", tempChild.get(childPosition).getMean()));

        convertView.setTag(tempChild.get(childPosition));
        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<ApGaussianRecord>) Childtem.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return groupItem.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_scanned_aps,parent,false);
        }


        RobotoTextView twSSID = (RobotoTextView) convertView.findViewById(R.id.ssid);
        RobotoTextView twRSSI = (RobotoTextView) convertView.findViewById(R.id.rssi_level);

        twSSID.setText(groupItem.get(groupPosition).getBSSID() + "|" + groupItem.get(groupPosition).getSSID());
        twRSSI.setText(String.valueOf( String.format("%.2f", groupItem.get(groupPosition).getRSSI())) + "dB");
        convertView.setTag(groupItem.get(groupPosition));

        View ind = convertView.findViewById(R.id.explist_indicator);
        if (ind != null) {
            ImageView indicator = (ImageView) ind;
            if (getChildrenCount(groupPosition) == 0) {

                indicator.setVisibility(View.INVISIBLE);
            } else {
                indicator.setVisibility(View.VISIBLE);
                indicator.setImageResource(
                        isExpanded ? R.drawable.arrow_down : R.drawable.arrow_down);
            }
        }

        TextView tw = (TextView) convertView.findViewById(R.id.explist_bar);
        ImageView icon = (ImageView) convertView.findViewById(R.id.explist_icon);


        if (groupItem.get(groupPosition).getRSSI() < -80) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
        } else if (groupItem.get(groupPosition).getRSSI() < -60) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.OrangeRed));
        } else if (groupItem.get(groupPosition).getRSSI() < -40) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.YellowGreen));
        }else {
            tw.setBackgroundColor(context.getResources().getColor(R.color.Green));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
