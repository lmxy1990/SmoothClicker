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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.utils.ActionType;
import pylapp.smoothclicker.android.views.PointsListAdapter.Point;

import java.util.ArrayList;
import java.util.List;

public class ActionListActivity extends AppCompatActivity {

    public static final String EXTRA_POINTS = "pylapp.smoothclicker.android.views.ActionListActivity.EXTRA_POINTS";

    private List<Point> mPoints;
    private ExpandableListView mElvActions;
    private ActionListAdapter mAdapter;
    private int mSelectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        ArrayList<Integer> xyPoints = intent.getIntegerArrayListExtra(EXTRA_POINTS);

        mPoints = new ArrayList<>();
        if (xyPoints != null && xyPoints.size() > 0) {
            for (int i = 0; i < xyPoints.size(); i += 2) {
                int x = xyPoints.get(i);
                int y = xyPoints.get(i + 1);
                mPoints.add(new Point(x, y));
            }
        }

        mElvActions = findViewById(R.id.elvActions);
        mAdapter = new ActionListAdapter(this, mPoints);
        mElvActions.setAdapter(mAdapter);

        mElvActions.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                mSelectedPosition = groupPosition;
                return true;
            }
        });

        mElvActions.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                mSelectedPosition = groupPosition;
                return false;
            }
        });

        Button btnAddAction = findViewById(R.id.btnAddAction);
        btnAddAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoints.add(new Point(0, 0));
                mAdapter.notifyDataSetChanged();
                mElvActions.expandGroup(mPoints.size() - 1);
                Toast.makeText(ActionListActivity.this, R.string.action_added, Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDeleteAction = findViewById(R.id.btnDeleteAction);
        btnDeleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPosition >= 0 && mSelectedPosition < mPoints.size()) {
                    mPoints.remove(mSelectedPosition);
                    mSelectedPosition = -1;
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(ActionListActivity.this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActionListActivity.this, R.string.select_action_first, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnMoveUp = findViewById(R.id.btnMoveUp);
        btnMoveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPosition > 0) {
                    Point temp = mPoints.get(mSelectedPosition);
                    mPoints.set(mSelectedPosition, mPoints.get(mSelectedPosition - 1));
                    mPoints.set(mSelectedPosition - 1, temp);
                    mSelectedPosition--;
                    mAdapter.notifyDataSetChanged();
                    mElvActions.expandGroup(mSelectedPosition);
                } else {
                    Toast.makeText(ActionListActivity.this, R.string.cannot_move_up, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnMoveDown = findViewById(R.id.btnMoveDown);
        btnMoveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPosition >= 0 && mSelectedPosition < mPoints.size() - 1) {
                    Point temp = mPoints.get(mSelectedPosition);
                    mPoints.set(mSelectedPosition, mPoints.get(mSelectedPosition + 1));
                    mPoints.set(mSelectedPosition + 1, temp);
                    mSelectedPosition++;
                    mAdapter.notifyDataSetChanged();
                    mElvActions.expandGroup(mSelectedPosition);
                } else {
                    Toast.makeText(ActionListActivity.this, R.string.cannot_move_down, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnClearAll = findViewById(R.id.btnClearAll);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoints.clear();
                mSelectedPosition = -1;
                mAdapter.notifyDataSetChanged();
                Toast.makeText(ActionListActivity.this, R.string.all_cleared, Toast.LENGTH_SHORT).show();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndReturn();
            }
        });
    }

    @Override
    public void onBackPressed() {
        saveAndReturn();
    }

    private void saveAndReturn() {
        ArrayList<Integer> result = new ArrayList<>();
        for (Point p : mPoints) {
            result.add(p.x);
            result.add(p.y);
        }

        Intent intent = new Intent();
        intent.putIntegerArrayListExtra(EXTRA_POINTS, result);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}