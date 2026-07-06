/*
    MIT License

    Copyright (c) 2016  Pierre-Yves Lapersonne (Mail: dev@pylapersonne.info)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.utils.ActionType;
import pylapp.smoothclicker.android.views.PointsListAdapter.Point;

import java.util.Arrays;
import java.util.List;

public class ActionListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<Point> mPoints;

    public ActionListAdapter(Context context, List<Point> points) {
        mContext = context;
        mPoints = points;
    }

    @Override
    public int getGroupCount() {
        return mPoints.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mPoints.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mPoints.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Point point = mPoints.get(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_action_group, null);
        }

        TextView tvActionIndex = convertView.findViewById(R.id.tvActionIndex);
        tvActionIndex.setText(String.valueOf(groupPosition + 1));

        TextView tvActionType = convertView.findViewById(R.id.tvActionType);
        tvActionType.setText(point.actionType.getDescription());

        TextView tvActionCoords = convertView.findViewById(R.id.tvActionCoords);
        StringBuilder coords = new StringBuilder();
        coords.append("(").append(point.x).append(",").append(point.y).append(")");
        if (point.actionType == ActionType.SWIPE || point.actionType == ActionType.SWIPE_LONG_CLICK) {
            coords.append(" -> (").append(point.endX).append(",").append(point.endY).append(")");
        }
        if (point.duration > Point.DEFAULT_DURATION) {
            coords.append(" ").append(point.duration).append("ms");
        }
        tvActionCoords.setText(coords.toString());

        ImageView ivExpand = convertView.findViewById(R.id.ivExpand);
        if (isExpanded) {
            ivExpand.setImageResource(R.drawable.ic_expand_less);
        } else {
            ivExpand.setImageResource(R.drawable.ic_expand_more);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Point point = mPoints.get(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_action_child, null);
        }

        Spinner spinnerActionType = convertView.findViewById(R.id.spinnerActionType);
        List<ActionType> actionTypes = Arrays.asList(ActionType.values());
        List<String> actionTypeNames = Arrays.asList(
                mContext.getString(R.string.action_click),
                mContext.getString(R.string.action_long_click),
                mContext.getString(R.string.action_swipe),
                mContext.getString(R.string.action_swipe_long_click)
        );
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                mContext, android.R.layout.simple_spinner_item, actionTypeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActionType.setAdapter(spinnerAdapter);
        spinnerActionType.setSelection(point.actionType.ordinal());

        spinnerActionType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                point.actionType = actionTypes.get(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        EditText etStartX = convertView.findViewById(R.id.etStartX);
        etStartX.setText(String.valueOf(point.x));
        etStartX.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        point.x = Integer.parseInt(etStartX.getText().toString());
                    } catch (NumberFormatException e) {
                        point.x = 0;
                    }
                }
            }
        });

        EditText etStartY = convertView.findViewById(R.id.etStartY);
        etStartY.setText(String.valueOf(point.y));
        etStartY.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        point.y = Integer.parseInt(etStartY.getText().toString());
                    } catch (NumberFormatException e) {
                        point.y = 0;
                    }
                }
            }
        });

        EditText etEndX = convertView.findViewById(R.id.etEndX);
        if (point.endX != Point.UNDEFINED_X) {
            etEndX.setText(String.valueOf(point.endX));
        } else {
            etEndX.setText("");
        }
        etEndX.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        point.endX = Integer.parseInt(etEndX.getText().toString());
                    } catch (NumberFormatException e) {
                        point.endX = Point.UNDEFINED_X;
                    }
                }
            }
        });

        EditText etEndY = convertView.findViewById(R.id.etEndY);
        if (point.endY != Point.UNDEFINED_Y) {
            etEndY.setText(String.valueOf(point.endY));
        } else {
            etEndY.setText("");
        }
        etEndY.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        point.endY = Integer.parseInt(etEndY.getText().toString());
                    } catch (NumberFormatException e) {
                        point.endY = Point.UNDEFINED_Y;
                    }
                }
            }
        });

        EditText etDuration = convertView.findViewById(R.id.etDuration);
        etDuration.setText(String.valueOf(point.duration));
        etDuration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        point.duration = Long.parseLong(etDuration.getText().toString());
                    } catch (NumberFormatException e) {
                        point.duration = Point.DEFAULT_DURATION;
                    }
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}