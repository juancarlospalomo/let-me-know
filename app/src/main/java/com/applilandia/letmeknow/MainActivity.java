package com.applilandia.letmeknow;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.applilandia.letmeknow.views.Tile;

/**
 * Created by JuanCarlos on 13/02/2015.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Tile tile = (Tile) findViewById(R.id.tileTask);
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
        });

    }

}
