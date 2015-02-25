package com.applilandia.letmeknow;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applilandia.letmeknow.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

/*final Tile tile = (Tile) findViewById(R.id.tileTask);
        tile.setContentBackgroundColor(getResources().getColor(R.color.red));
        tile.setContentText("Expired");
        tile.setFooterPrimaryLine("Last Expired task");
        tile.setFooterSecondaryLine("date");
        tile.setFooterIcon(R.drawable.ic_alarm_off);
        tile.setContentOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        tile.setPivotX(tile.getRight());
        tile.setPivotY(tile.getTop());
        tile.animate().scaleX(0).scaleY(0);
        tile.animate().setDuration(1000);
        tile.animate().setInterpolator(new AccelerateDecelerateInterpolator(MainActivity.this, null));
        tile.animate().start();
        }
        });
        tile.setFooterTextOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Log.v("MainActivity", "FooterTextOnClickListener.onClick");
        }
        });
        tile.setIconOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Log.v("MainActivity", "FooterIconOnClickListener.onClick");
        }
        });*/


/**
 * Created by JuanCarlos on 13/02/2015.
 */
public class MainActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        createNavigationDrawer();
        addHomeFragment();
    }

    /**
     * Create and init the navigation drawer
     */
    private void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case DrawerListAdapter.SETTINGS_INDEX:
                        Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerToggle.syncState();
    }

    private void addHomeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_frame, new HomeFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newChange) {
        super.onConfigurationChanged(newChange);
        mDrawerToggle.onConfigurationChanged(newChange);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItem {
        public int iconResId;
        public int textResId;

        private DrawerItem(int iconResId, int textResId) {
            this.iconResId = iconResId;
            this.textResId = textResId;
        }
    }

    private class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

        public final static int SETTINGS_INDEX = 0;

        private List<DrawerItem> mDrawerListItems;

        private DrawerListAdapter(Context context) {
            super(context, R.layout.drawer_list_item);
            mDrawerListItems = new ArrayList<>();
            mDrawerListItems.add(new DrawerItem(R.drawable.ic_settings, R.string.menu_settings));
        }


        @Override
        public int getCount() {
            return mDrawerListItems.size();
        }

        @Override
        public DrawerItem getItem(int position) {
            return mDrawerListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
            switch (position) {
                case SETTINGS_INDEX:
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.imageDrawerAction);
                    imageView.setImageResource(mDrawerListItems.get(position).iconResId);
                    TextView textView = (TextView) convertView.findViewById(R.id.textViewDrawerAction);
                    textView.setText(mDrawerListItems.get(position).textResId);
                    break;
                default:
                    super.getView(position, convertView, parent);
            }
            return convertView;
        }
    }


}
