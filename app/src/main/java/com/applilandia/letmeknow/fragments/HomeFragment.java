package com.applilandia.letmeknow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.loaders.SummaryLoader;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.views.Tile;

import java.util.List;

/**
 * Created by JuanCarlos on 24/02/2015.
 * Fragment that displays the home activity screen
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    private final static int LOADER_SUMMARY = 1;

    private ProgressBar mProgressBar;
    private GridView mGridView;
    private GridTilesAdapter mAdapter; //Adapter for linking to GridView

    /**
     * Creates and returns the view hierarchy associated with the fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Tells the fragment that its activity has completed its own Activity.onCreated()
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Once the fragment is attached to the activity, we can hide it
        //because the window token is already available
        hideSoftKeyboard();
        View view = getView();
        //Get views objects
        mGridView = (GridView) view.findViewById(R.id.gridTiles);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressActivityMain);
        getLoaderManager().initLoader(LOADER_SUMMARY, null, this);
    }

    /**
     * Hide the input keyboard method
     */
    private void hideSoftKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
/*
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
*/
    }

    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new SummaryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = new GridTilesAdapter(data);
            mGridView.setAdapter(mAdapter);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = null;
            mGridView.setAdapter(null);
        }
    }

    /**
     * Class for ViewHolder Pattern
     */
    private static class ViewHolder {
        Tile tile;
    }

    /**
     * Adapter for the Grid
     */
    private class GridTilesAdapter extends BaseAdapter {

        private final static int INDEX_TILE_EXPIRED = 0;
        private final static int INDEX_TILE_TODAY = 1;
        private final static int INDEX_TILE_FUTURE = 2;
        private final static int INDEX_TILE_ANYTIME = 3;

        private final String LOG_TAG = GridTilesAdapter.class.getSimpleName();

        //Task List of the Adapter
        private List<Task> mTaskList;

        private GridTilesAdapter(List<Task> mTaskList) {
            this.mTaskList = mTaskList;
        }

        @Override
        public int getCount() {
            return mTaskList.size();
        }

        @Override
        public Task getItem(int position) {
            return mTaskList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mTaskList.get(position)!=null) {
                return mTaskList.get(position)._id;
            } else {
                //TODO: Remove Log
                Log.v(LOG_TAG, String.valueOf(position));
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.grid_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tile = (Tile) convertView.findViewById(R.id.tileTask);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tile.setContentText(getResources().getStringArray(R.array.text_tile_content)[position]);
            viewHolder.tile.setContentBackgroundColor(getColor(position));
            Task task = mTaskList.get(position);
            if (task != null) {
                viewHolder.tile.setFooterPrimaryLine(task.name);
                if ((task.targetDateTime != null) && (!task.targetDateTime.isNull())) {
                    viewHolder.tile.setFooterSecondaryLine(task.targetDateTime.getDisplayFormat(getActivity()));
                }
                if (task.getCurrentNotificationsCount()>0) {
                    viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_on);
                } else {
                    viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_off);
                }
            } else {
                String[] textDefaults = getResources().getStringArray(R.array.text_defaults_primary_line);
                viewHolder.tile.setFooterPrimaryLine(textDefaults[position]);
            }
            viewHolder.tile.setContentOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "tile", Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.tile.setFooterTextOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "tile", Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.tile.setIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "tile", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }

        /**
         * Returns the color according to the required tile position
         * @param position position in the adapter
         * @return color
         */
        private int getColor(int position) {
            switch (position) {
                case INDEX_TILE_EXPIRED:
                    return getResources().getColor(R.color.tile_expired_content_background);
                case INDEX_TILE_TODAY:
                    return getResources().getColor(R.color.tile_today_content_background);
                case INDEX_TILE_FUTURE:
                    return getResources().getColor(R.color.tile_future_content_background);
                case INDEX_TILE_ANYTIME:
                    return getResources().getColor(R.color.tile_anytime_content_background);
                default:
                    return getResources().getColor(R.color.tile_expired_content_background);
            }
        }
    }


}
