package com.kelsos.mbrc.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import com.kelsos.mbrc.BuildConfig;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.adapters.DrawerAdapter;
import com.kelsos.mbrc.constants.UserInputEventType;
import com.kelsos.mbrc.controller.Controller;
import com.kelsos.mbrc.data.NavigationEntry;
import com.kelsos.mbrc.enums.DisplayFragment;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.events.ui.ConnectionStatusChange;
import com.kelsos.mbrc.events.ui.DrawerEvent;
import com.kelsos.mbrc.utilities.RemoteUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.fragment.RoboListFragment;
import roboguice.inject.InjectView;

import java.util.ArrayList;


public class DrawerFragment extends RoboListFragment implements FragmentManager.OnBackStackChangedListener {
    @Inject Bus bus;
    @Inject RemoteUtils rmUtils;
    @InjectView(R.id.menuConnector) TextView menuConnector;
    @InjectView(R.id.drawer_version_indicator) TextView versionIndicator;
    @InjectView(R.id.menu_exit) TextView menuExit;

    private Typeface robotoLight;
    private DrawerLayout mDrawerLayout;
    private int mSelection;
    private boolean mBackstackChanging;

    private TextView.OnLongClickListener connectButtonLongClick = new TextView.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            bus.post(new MessageEvent(UserInputEventType.ResetConnection));
            return false;
        }
    };

    private TextView.OnClickListener connectButtonClick = new TextView.OnClickListener() {

        public void onClick(View v) {
            bus.post(new MessageEvent(UserInputEventType.StartConnection));
        }
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/roboto_light.ttf");
        mSelection = 0;
        mBackstackChanging = false;

        if (savedInstanceState != null) {
            mSelection = savedInstanceState.getInt("mSelection");
        }
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ui_fragment_drawer, container, false);
    }

    @Override public void onStart() {
        super.onStart();
        bus.register(this);
        menuConnector.setOnClickListener(connectButtonClick);
        menuConnector.setOnLongClickListener(connectButtonLongClick);
        menuConnector.setTypeface(robotoLight);

        menuExit.setTypeface(robotoLight);
        menuExit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Activity activity = getActivity();
                activity.stopService(new Intent(activity, Controller.class));
                activity.finish();
            }
        });

        ArrayList<NavigationEntry> nav = new ArrayList<>();
        nav.add(new NavigationEntry("Home"));
        nav.add(new NavigationEntry("Search"));
        nav.add(new NavigationEntry("Now playing list"));
        nav.add(new NavigationEntry("Lyrics"));

        setListAdapter(new DrawerAdapter(getActivity(), R.layout.ui_drawer_item, nav));
        getListView().setOnItemClickListener(new DrawerOnClickListener());
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setItemChecked(mSelection, true);

        if (mDrawerLayout == null) {
            mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        }
        try {
            versionIndicator.setText(String.format(getString(R.string.ui_drawer_menu_version), RemoteUtils.getVersion(getActivity())));
        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Subscribe public void handleConnectionStatusChange(final ConnectionStatusChange change) {
        if (menuConnector == null) return;
        switch (change.getStatus()) {
            case CONNECTION_OFF:
                menuConnector.setText(R.string.drawer_connection_status_off);
                break;
            case CONNECTION_ON:
                menuConnector.setText(R.string.drawer_connection_status_on);
                break;
            case CONNECTION_ACTIVE:
                menuConnector.setText(R.string.drawer_connection_status_active);
                break;
        }
    }

    @Override public void onBackStackChanged() {
        if (!mBackstackChanging)
            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mSelection = 0;
                getListView().setItemChecked(mSelection, true);
            }
        mBackstackChanging = false;

    }

    private class DrawerOnClickListener implements ListView.OnItemClickListener {

        @Override public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
            mBackstackChanging = true;
            DrawerEvent dEvent;
            if (mSelection != i) {

                getListView().setItemChecked(i, true);
                mSelection = i;

                DisplayFragment dfrag = DisplayFragment.values()[i];
                dEvent = new DrawerEvent(dfrag);
            } else {
                dEvent = new DrawerEvent();
            }

            bus.post(dEvent);
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mSelection", mSelection);
        super.onSaveInstanceState(outState);
    }
}