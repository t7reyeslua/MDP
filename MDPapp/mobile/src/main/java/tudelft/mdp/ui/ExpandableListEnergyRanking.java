package tudelft.mdp.ui;

import com.devspark.robototextview.widget.RobotoTextView;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.DeviceUsageRecord;
import tudelft.mdp.enums.Energy;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.utils.Utils;


public class ExpandableListEnergyRanking extends BaseExpandableListAdapter {

    public ArrayList<DeviceUsageRecord> tempChild;
    public ArrayList<DeviceUsageRecord> groupItem = new ArrayList<DeviceUsageRecord>();
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

    public ExpandableListEnergyRanking(Context context, ArrayList<DeviceUsageRecord> grList,
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
        tempChild = (ArrayList<DeviceUsageRecord>) Childtem.get(groupPosition);
        TextView text = null;
        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_subitem_energy_ranking,parent,false);
        }

        RobotoTextView twName = (RobotoTextView) convertView.findViewById(R.id.name);
        RobotoTextView twKWH = (RobotoTextView) convertView.findViewById(R.id.energyKWH);
        RobotoTextView twEUR = (RobotoTextView) convertView.findViewById(R.id.energyEUR);

        Double energy = tempChild.get(childPosition).getUserTime();


        String user = tempChild.get(childPosition).getUsername();
        if (user.length() > 22){
            user = user.substring(0,21) + ".";
        }

        Log.i("Rankings", "Child:" + user + "|" + energy + "|" + String.valueOf(String.format("%.2f", Energy.KWH_EURO * energy)) + " \u20ac");
        twName.setText(user);
        twKWH.setText(String.valueOf(String.format("%.0f",energy)) + " kWh");
        twEUR.setText(String.valueOf(String.format("%.2f", Energy.KWH_EURO * energy)) + " \u20ac");

        TextView tw = (TextView) convertView.findViewById(R.id.explist_bar);

        Double userDailyLimit = Double.valueOf(
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(UserPreferences.TARGET_KWH_INDIVIDUAL, "10.0"));
        Double userWeeklyLimit = userDailyLimit * 7;
        Double userMonthlyLimit = userDailyLimit * 30;

        tw.setBackgroundColor(context.getResources().getColor(R.color.Green));
        if (tempChild.get(childPosition).getTimespan().equals(UserPreferences.TODAY)) {
            if (energy > userDailyLimit) {
                tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
            }
        } else if (tempChild.get(childPosition).getTimespan().equals(UserPreferences.WEEK)) {
            if (energy > userWeeklyLimit) {
                tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
            }
        } else if (tempChild.get(childPosition).getTimespan().equals(UserPreferences.MONTH)) {
            if (energy > userMonthlyLimit) {
                tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
            }
        }


        convertView.setTag(tempChild.get(childPosition));
        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<DeviceUsageRecord>) Childtem.get(groupPosition)).size();
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
            convertView = layoutInflater.inflate(R.layout.list_item_energy_ranking_group,parent,false);
        }


        RobotoTextView twTimespan = (RobotoTextView) convertView.findViewById(R.id.timespan);
        RobotoTextView twKWH = (RobotoTextView) convertView.findViewById(R.id.energyKWH);
        RobotoTextView twEUR = (RobotoTextView) convertView.findViewById(R.id.energyEUR);

        Double energy = groupItem.get(groupPosition).getUserTime();



        twTimespan.setText(Utils.getNameOfTimespan(groupItem.get(groupPosition).getTimespan()));

        if (energy != null) {
            Log.i("Rankings", "Group:" + Utils.getNameOfTimespan(groupItem.get(groupPosition).getTimespan()) + "|" + energy + "|" + String.valueOf(String.format("%.2f", Energy.KWH_EURO * energy)) + " \u20ac");

            twKWH.setText(String.valueOf(String.format("%.0f", energy)) + " kWh");
            twEUR.setText(
                    String.valueOf(String.format("%.2f", Energy.KWH_EURO * energy)) + " \u20ac");
        }

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

        Double groupDailyLimit = Double.valueOf(
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(UserPreferences.TARGET_KWH_GROUP, "40.0"));
        Double groupWeeklyLimit = groupDailyLimit * 7;
        Double groupMonthlyLimit = groupDailyLimit * 30;

        if (energy != null) {
            tw.setBackgroundColor(context.getResources().getColor(R.color.Green));
            if (groupItem.get(groupPosition).getTimespan().equals(UserPreferences.TODAY)) {
                if (energy > groupDailyLimit) {
                    tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
                }
            } else if (groupItem.get(groupPosition).getTimespan().equals(UserPreferences.WEEK)) {
                if (energy > groupWeeklyLimit) {
                    tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
                }
            } else if (groupItem.get(groupPosition).getTimespan().equals(UserPreferences.MONTH)) {
                if (energy > groupMonthlyLimit) {
                    tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
                }
            }
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