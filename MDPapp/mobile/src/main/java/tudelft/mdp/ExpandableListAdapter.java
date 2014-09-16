package tudelft.mdp;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	public ArrayList<String> groupItem, tempChild;
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

	public ExpandableListAdapter(Context context, ArrayList<String> grList,
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
		tempChild = (ArrayList<String>) Childtem.get(groupPosition);
		TextView text = null;
                if (convertView == null)
                {
                    LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(R.layout.list_subitem_drawer,parent,false);
                }
		text = (TextView) convertView;
		text.setText(tempChild.get(childPosition));
		convertView.setTag(tempChild.get(childPosition));
		return convertView;
	}

	

	@Override
	public int getChildrenCount(int groupPosition) {
		return ((ArrayList<String>) Childtem.get(groupPosition)).size();
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
                convertView = layoutInflater.inflate(R.layout.list_item_drawer,parent,false);
            }


            TextView textView = (TextView) convertView.findViewById(R.id.group_name);
            textView.setText(groupItem.get(groupPosition));
            convertView.setTag(groupItem.get(groupPosition));

            View ind = convertView.findViewById(R.id.explist_indicator);
            if (ind != null) {
                ImageView indicator = (ImageView) ind;
                if (getChildrenCount(groupPosition) == 0) {

                    indicator.setVisibility(View.INVISIBLE);
                } else {
                    indicator.setVisibility(View.VISIBLE);
                    indicator.setImageResource(
                            isExpanded ? R.drawable.ic_action_collapse : R.drawable.ic_action_expand);
                }
            }

            TextView tw = (TextView) convertView.findViewById(R.id.explist_bar);
            ImageView icon = (ImageView) convertView.findViewById(R.id.explist_icon);
            switch (groupPosition) {
                case NavigationDrawer.DASHBOARD:
                    tw.setBackgroundColor(context.getResources().getColor(R.color.Crimson));
                    icon.setImageResource(R.drawable.ic_action_labels);
                    break;
                case NavigationDrawer.ACTIVITYMONITOR:
                    tw.setBackgroundColor(context.getResources().getColor(R.color.DarkOrchid));
                    icon.setImageResource(R.drawable.ic_action_person);
                    break;
                case NavigationDrawer.LOCATIONTRACKER:
                    tw.setBackgroundColor(context.getResources().getColor(R.color.DarkCyan));
                    icon.setImageResource(R.drawable.ic_action_place);
                    break;
                case NavigationDrawer.UTILITIES:
                    tw.setBackgroundColor(context.getResources().getColor(R.color.Gold));
                    icon.setImageResource(R.drawable.settings3);
                    break;
                default:
                    break;
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
