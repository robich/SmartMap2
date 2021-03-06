package ch.epfl.smartmap.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.FriendsPositionsThread;
import ch.epfl.smartmap.background.InvitationsService;
import ch.epfl.smartmap.background.NearEventsThread;
import ch.epfl.smartmap.background.OwnPositionService;
import ch.epfl.smartmap.background.ServiceContainer;
import ch.epfl.smartmap.background.UpdateDatabaseThread;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.cache.Displayable;
import ch.epfl.smartmap.cache.Event;
import ch.epfl.smartmap.cache.Invitation;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.gui.SearchLayout;
import ch.epfl.smartmap.gui.SideMenu;
import ch.epfl.smartmap.gui.SlidingPanel;
import ch.epfl.smartmap.listeners.AddEventOnMapLongClickListener;
import ch.epfl.smartmap.listeners.CacheListener;
import ch.epfl.smartmap.map.DefaultMarkerManager;
import ch.epfl.smartmap.map.DefaultZoomManager;
import ch.epfl.smartmap.util.Utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * This Activity displays the core features of the App. It dispays the
 * {@code GoogleMap}, contains the {@code SideMenu}, as well as the
 * {@code SearchLayout}. Starts {@code FriendsPositionsThread},
 * {@code NearEventsThread} and {@code UpdateDatabaseThread} in its
 * {@code onCreate} method.
 * 
 * @author jfperren
 * @author hugo-S
 * @author SpicyCH
 * @author agpmilli
 */

public class MainActivity extends FragmentActivity implements CacheListener {

    /**
     * Types of Menu that can be displayed on this activity
     * 
     * @author jfperren
     */
    private enum MenuTheme {
        MAP,
        SEARCH,
        ITEM;
    }

    /**
     * A listener that reset events markers colors and info panel when clicking
     * on map
     * 
     * @author hugo-S
     */
    private class ResetMarkerColorAndInfoPannelOnMapClick implements OnMapClickListener {

        /*
         * (non-Javadoc)
         * @see
         * com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com
         * .google.android.gms.maps
         * .model.LatLng)
         */
        @Override
        public void onMapClick(LatLng arg0) {

            MainActivity.this.setMainMenu();
            mEventMarkerManager.resetMarkersIcon(MainActivity.this);

        }

    }

    /**
     * A listener that shows info in action bar when a marker is clicked on
     * 
     * @author hugo-S
     */
    private class ShowInfoOnMarkerClick implements OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker arg0) {

            if (mFriendMarkerManager.isDisplayedMarker(arg0)) {
                Displayable itemClicked = mFriendMarkerManager.getItemForMarker(arg0);
                mMapZoomer.centerOnLocation(arg0.getPosition());
                MainActivity.this.setItemMenu(itemClicked);
                return true;
            } else if (mEventMarkerManager.isDisplayedMarker(arg0)) {
                Displayable itemClicked = mEventMarkerManager.getItemForMarker(arg0);
                mMapZoomer.centerOnLocation(arg0.getPosition());
                MainActivity.this.setItemMenu(itemClicked);
                mEventMarkerManager.resetMarkersIcon(MainActivity.this);
                arg0.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                arg0.setSnippet(DefaultMarkerManager.MarkerColor.RED.toString());
                return true;
            }
            return false;
        }
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GOOGLE_PLAY_REQUEST_CODE = 10;
    // Indexes in ActionBar of different menuItems
    private static final int MENU_ITEM_SEARCHBAR_INDEX = 0;

    private static final int MENU_ITEM_NOTIFICATION_INDEX = 1;
    private static final int MENU_ITEM_CLOSE_SEARCH_INDEX = 2;
    private static final int MENU_ITEM_OPEN_INFO_INDEX = 3;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private SideMenu mSideMenu;

    private GoogleMap mGoogleMap;
    private DefaultMarkerManager mFriendMarkerManager;
    private DefaultMarkerManager mEventMarkerManager;

    private DefaultZoomManager mMapZoomer;
    private SupportMapFragment mFragmentMap;

    private Menu mMenu;

    // Informations about the menu being displayed
    private MenuTheme mMenuTheme;

    private Displayable mCurrentItem;

    private LayerDrawable mIcon;

    private FriendsPositionsThread mFriendsPosThread;

    /**
     * Display the map with the current location
     */
    public void displayMap() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getBaseContext());
        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, GOOGLE_PLAY_REQUEST_CODE);
            dialog.show();
        } else {
            // Google Play Services are available.
            // Getting reference to the SupportMapFragment of activity_main.xml
            mFragmentMap = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting GoogleMap object from the fragment
            mGoogleMap = mFragmentMap.getMap();
            // Enabling MyLocation Layer of Google Map
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    public MenuTheme getMenuTheme() {
        return mMenuTheme;
    }

    private void initializeMarkers() {
        if ((mFriendMarkerManager != null) && (mEventMarkerManager != null)) {
            mFriendMarkerManager.updateMarkers(this, new HashSet<Displayable>(ServiceContainer.getCache()
                .getAllVisibleFriends()));
            mEventMarkerManager.updateMarkers(this, new HashSet<Displayable>(ServiceContainer.getCache()
                .getAllVisibleEvents()));
            for (Marker marker : mEventMarkerManager.getDisplayedMarkers()) {
                marker.setSnippet(DefaultMarkerManager.MarkerColor.ORANGE.toString());
            }
        } else {
            Log.e(TAG, "The friend marcker or the event marcker was null");
        }
    }

    @Override
    public void onBackPressed() {
        switch (mMenuTheme) {
            case MAP:
                break;
            case SEARCH:
                break;
            case ITEM:
                this.setMainMenu();
                mEventMarkerManager.resetMarkersIcon(this);
                break;
            default:
                assert false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ServiceContainer.initSmartMapServices(this);
        // starting the background service
        this.startService(new Intent(this, InvitationsService.class));
        this.startService(new Intent(this, OwnPositionService.class));

        // Set actionbar color
        this.getActionBar().setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.main_blue)));
        this.getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        this.getActionBar().setHomeAsUpIndicator(this.getResources().getDrawable(R.drawable.ic_drawer));
        mMenuTheme = MenuTheme.MAP;

        // Get needed Views
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) this.findViewById(R.id.left_drawer_listView);

        mSideMenu = new SideMenu(this);
        mSideMenu.initializeDrawerLayout();

        if (savedInstanceState == null) {
            this.displayMap();
        }

        if (mGoogleMap != null) {
            // Set different tools for the GoogleMap
            mFriendMarkerManager = new DefaultMarkerManager(mGoogleMap);
            mEventMarkerManager = new DefaultMarkerManager(mGoogleMap);
            mMapZoomer = new DefaultZoomManager(mFragmentMap);
            // Adds markers
            this.initializeMarkers();
            this.zoomAccordingToAllMarkers();

            // Add listeners to the GoogleMap
            mGoogleMap.setOnMapLongClickListener(new AddEventOnMapLongClickListener(this));
            mGoogleMap.setOnMarkerClickListener(new ShowInfoOnMarkerClick());
            mGoogleMap.setOnMapClickListener(new ResetMarkerColorAndInfoPannelOnMapClick());
        }

        ServiceContainer.getCache().addOnCacheListener(this);

        mFriendsPosThread = new FriendsPositionsThread();
        mFriendsPosThread.start();
        new UpdateDatabaseThread().start();
        new NearEventsThread().start();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.listeners.CacheListener#onFilterListUpdate()
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.main, menu);

        ServiceContainer.initSmartMapServices(this);

        // Get menu
        mMenu = menu;
        // Get the notifications MenuItem and
        // its LayerDrawable (layer-list)
        MenuItem item = mMenu.findItem(R.id.action_notifications);
        mIcon = (LayerDrawable) item.getIcon();

        // Get Views
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        final SearchLayout mSearchLayout = (SearchLayout) this.findViewById(R.id.search_layout);
        final MainActivity thisActivity = this;

        searchItem.setOnActionExpandListener(new OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                thisActivity.setMainMenu();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Open Sliding Panel and Displays the main search view
                thisActivity.setSearchMenu();
                mSearchLayout.resetView("");
                return true;
            }
        });

        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Give the query results to searchLayout
                mSearchLayout.setSearchQuery(mSearchView.getQuery().toString());
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return false;
            }
        });

        // Update notif count
        this.updateNotifCountBadge();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        ServiceContainer.getDatabase().updateFromCache();
        Log.d(TAG, "Updated Database");
        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.listeners.CacheListener#onFriendListUpdate()
     */
    @Override
    public void onEventListUpdate() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEventMarkerManager.updateMarkers(MainActivity.this, new HashSet<Displayable>(ServiceContainer
                    .getCache().getAllVisibleEvents()));
                MainActivity.this.updateItemMenu();
            }
        });
    }

    @Override
    public void onFilterListUpdate() {
        // Nothing
    }

    @Override
    public void onInvitationListUpdate() {
        // Update LayerDrawable's BadgeDrawable
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.updateNotifCountBadge();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get ID of MenuItem
        int id = item.getItemId();
        // Handle Click according to ID
        switch (id) {
            case android.R.id.home:
                switch (mMenuTheme) {
                    case MAP:
                        if (mDrawerList.isShown()) {
                            mDrawerLayout.closeDrawer(mDrawerList);
                        } else {
                            mDrawerLayout.openDrawer(mDrawerList);
                        }
                        break;
                    case SEARCH:
                        this.setMainMenu();
                        break;
                    case ITEM:
                        this.setMainMenu();
                        break;
                    default:
                        assert false;
                }
                break;
            case R.id.action_notifications:
                this.setNotificationBadgeCountTo0();
                return true;
            case R.id.action_hide_search:
                this.setMainMenu();
                return true;
            case R.id.action_item_more:
                this.openInformationActivity();
                return true;
            default:
                assert false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFriendsPosThread.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFriendsPosThread.enable();
        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLongClickListener(new AddEventOnMapLongClickListener(this));
        }
        // get Intent that started this Activity
        Intent startingIntent = this.getIntent();
        // get the value of the user string
        Location eventLocation = startingIntent.getParcelableExtra(AddEventActivity.LOCATION_EXTRA);
        if (eventLocation != null) {
            mMapZoomer.zoomWithAnimation(new LatLng(eventLocation.getLatitude(), eventLocation.getLongitude()));
            eventLocation = null;
        }

        // Set menu Style
        switch (mMenuTheme) {
            case SEARCH:
                this.setSearchMenu();
                break;
            case ITEM:
                this.setItemMenu(mCurrentItem);
                break;
            case MAP:
                break;
            default:
                assert false;
        }

        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLongClickListener(new AddEventOnMapLongClickListener(this));
        }

        this.zoomAccordingToAllMarkers();
    }

    @Override
    public void onUserListUpdate() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFriendMarkerManager.updateMarkers(MainActivity.this, new HashSet<Displayable>(ServiceContainer
                    .getCache().getAllVisibleFriends()));
                MainActivity.this.updateItemMenu();
            }
        });
    }

    /**
     * Opens Information Activity (called from MenuItem on Item view)
     * 
     * @author jfperren
     */
    public void openInformationActivity() {
        if (mCurrentItem instanceof User) {
            Intent intent = new Intent(this, UserInformationActivity.class);
            intent.putExtra("USER", mCurrentItem.getId());
            this.startActivity(intent);
        } else if (mCurrentItem instanceof Event) {
            Intent intent = new Intent(this, EventInformationActivity.class);
            intent.putExtra("EVENT", mCurrentItem.getId());
            this.startActivity(intent);
        }
    }

    /**
     * Zoom on item and sets Item menu
     * 
     * @param item
     */
    public void performQuery(Displayable item) {
        // Focus on Friend & Sets menu
        mMapZoomer.zoomWithAnimation(item.getLatLng());
        this.setItemMenu(item);
    }

    /**
     * Sets the {@code ActionBar} to display informations about a Displayable
     * Item. It sets the Title,
     * Subtitle and photo to those of the item.
     * 
     * @param item
     *            Item to be displayed
     */
    public void setItemMenu(final Displayable item) {
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        // Closes panel and change only if panel could close
        if (mSearchPanel.close() || mSearchPanel.isClosed()) {
            // Set visibility of MenuItems
            mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).getActionView().clearFocus();
            mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).collapseActionView();
            mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).setVisible(true);
            mMenu.getItem(MENU_ITEM_NOTIFICATION_INDEX).setVisible(false);
            mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(false);
            mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(true);
            // Change ActionBar title and icon
            final ActionBar actionBar = this.getActionBar();
            actionBar.setTitle(item.getTitle());
            actionBar.setSubtitle(item.getSubtitle());
            actionBar.setIcon(new BitmapDrawable(this.getResources(), item.getActionImage()));
            // ActionBar HomeIndicator
            actionBar.setHomeAsUpIndicator(null);

            mCurrentItem = item;
            mMenuTheme = MenuTheme.ITEM;
        }
    }

    /**
     * Sets the main Menu of the Activity, which contains a search icon and the
     * notification badge.
     */
    public void setMainMenu() {
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        // Closes panel and change only if panel could close
        if (mSearchPanel.close() || mSearchPanel.isClosed()) {
            // Collapse searchBar if needed
            if ((mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).getActionView() != null)
                && mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).isActionViewExpanded()) {
                mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).collapseActionView();
            }
            // Set visibility of MenuItems
            mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).setVisible(true);
            mMenu.getItem(MENU_ITEM_NOTIFICATION_INDEX).setVisible(true);
            mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(false);
            mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(false);
            // Change ActionBar title and icon
            ActionBar actionBar = this.getActionBar();
            actionBar.setTitle(R.string.app_name);
            actionBar.setSubtitle(null);
            actionBar.setIcon(R.drawable.ic_launcher);
            actionBar.setHomeAsUpIndicator(this.getResources().getDrawable(R.drawable.ic_drawer));
            mMenuTheme = MenuTheme.MAP;
        }
    }

    /**
     * Set the notification badge to 0
     */
    private void setNotificationBadgeCountTo0() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update LayerDrawable's BadgeDrawable
                Utils.setBadgeCount(MainActivity.this, mIcon, 0);
            }
        });
        Intent pNotifIntent = new Intent(this, InvitationPanelActivity.class);
        this.startActivity(pNotifIntent);
    }

    /**
     * Sets the {@code ActionBar} to search theme. The {@code SearchPanel}
     * slides up, the searchBar collapses
     * and the {@code MenuItem} to close the search appears.
     */
    public void setSearchMenu() {
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        // Closes panel and change only if panel could close
        if (mSearchPanel.open()) {
            // Set visibility of MenuItems
            mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).setVisible(true);
            mMenu.getItem(MENU_ITEM_NOTIFICATION_INDEX).setVisible(false);
            mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(true);
            mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(false);
            // Change ActionBar title and icon
            ActionBar actionBar = this.getActionBar();
            actionBar.setTitle(R.string.app_name);
            actionBar.setSubtitle(null);
            actionBar.setIcon(R.drawable.ic_launcher);
            mMenuTheme = MenuTheme.SEARCH;
        }
    }

    /**
     * Sets the Title, Subtitle and Image of the {@code ActionBar} with the
     * informations about another
     * Displayable item.
     */
    public void updateItemMenu() {
        if (mMenuTheme == MenuTheme.ITEM) {
            ActionBar actionBar = this.getActionBar();
            actionBar.setTitle(mCurrentItem.getTitle());
            actionBar.setSubtitle(mCurrentItem.getSubtitle());
            actionBar.setIcon(new BitmapDrawable(this.getResources(), mCurrentItem.getActionImage()));
        }
    }

    /**
     * Update the number badge on notification icon
     */
    public void updateNotifCountBadge() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update LayerDrawable's BadgeDrawable
                Utils.setBadgeCount(MainActivity.this, mIcon,
                    ServiceContainer.getCache().getInvitations(new Cache.SearchFilter<Invitation>() {
                        @Override
                        public boolean filter(Invitation item) {
                            // Get Unread invitations
                            return item.getStatus() == Invitation.UNREAD;
                        }
                    }).size());
            }
        });
    }

    private void zoomAccordingToAllMarkers() {

        List<Marker> allMarkers = new ArrayList<Marker>(mFriendMarkerManager.getDisplayedMarkers());
        allMarkers.addAll(mEventMarkerManager.getDisplayedMarkers());

        Intent startingIntent = this.getIntent();
        if (startingIntent.getParcelableExtra(AddEventActivity.LOCATION_EXTRA) == null) {
            mMapZoomer.zoomAccordingToMarkers(allMarkers);
        }
    }
}