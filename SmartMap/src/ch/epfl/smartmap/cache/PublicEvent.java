package ch.epfl.smartmap.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.ServiceContainer;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An event that can be seen on the map
 * 
 * @author ritterni
 * @author SpicyCH (PublicEvents are now Parcelable)
 */

public class PublicEvent implements Event {

    // Mandatory fields
    private long mId;
    private String mName;
    private long mCreatorId;
    private GregorianCalendar mStartDate;
    private GregorianCalendar mEndDate;
    private Location mLocation;
    // Optional fields
    private String mDescription;
    private String mLocationString;

    private List<Long> mParticipants;

    public final static int DEFAULT_ICON = R.drawable.default_event;

    public static final float MARKER_ANCHOR_X = (float) 0.5;
    public static final float MARKER_ANCHOR_Y = 1;

    protected PublicEvent(ImmutableEvent event) {
        if (event.getID() < -1) {
            throw new IllegalArgumentException();
        } else {
            mId = event.getID();
        }

        if ((event.getName() == null) || event.getName().equals("")) {
            throw new IllegalArgumentException();
        } else {
            mName = event.getName();
        }

        if (event.getCreatorId() < 0) {
            throw new IllegalArgumentException();
        } else {
            mCreatorId = event.getCreatorId();
        }

        if ((event.getStartDate() == null) || (event.getEndDate() == null)) {
            throw new IllegalArgumentException();
        } else {
            mStartDate = new GregorianCalendar(TimeZone.getDefault());
            mEndDate = new GregorianCalendar(TimeZone.getDefault());
        }

        if (event.getLocation() == null) {
            throw new IllegalArgumentException();
        } else {
            mLocation = new Location(event.getLocation());
        }

        if (event.getLocationString() == null) {
            mLocationString = Displayable.NO_LOCATION_STRING;
        } else {
            mLocationString = event.getLocationString();
        }

        if (event.getDescription() == null) {
            mDescription = Event.NO_DESCRIPTION;
        } else {
            mDescription = event.getDescription();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (this.getClass() == obj.getClass())
            && (this.getId() == ((PublicEvent) obj).getId());
    }

    @Override
    public long getCreatorId() {
        return mCreatorId;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public Calendar getEndDate() {
        return (Calendar) mEndDate.clone();
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public Bitmap getImage() {
        return Event.DEFAULT_IMAGE;
    }

    @Override
    public ImmutableEvent getImmutableCopy() {
        return new ImmutableEvent(mId, mName, mCreatorId, mDescription, mStartDate, mEndDate, mLocation,
            mLocationString, mParticipants);
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#getLatLng()
     */
    @Override
    public LatLng getLatLng() {
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#getLocationString()
     */
    @Override
    public String getLocationString() {
        return mLocationString;
    }

    /*
     * (non-Javadoc)
     * @see
     * ch.epfl.smartmap.cache.Displayable#getMarkerOptions(android.content.Context
     * )
     * @author hugo-S
     */
    @Override
    public MarkerOptions getMarkerOptions(Context context) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
            .title(this.getName())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y);
        return markerOptions;

    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public List<Long> getParticipants() {
        return new ArrayList<Long>(mParticipants);
    }

    @Override
    public GregorianCalendar getStartDate() {
        return mStartDate;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Displayable#getSubtitle()
     */
    @Override
    public String getSubtitle() {
        return mDescription;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Displayable#getTitle()
     */
    @Override
    public String getTitle() {
        return mName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) mId;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isGoing()
     */
    @Override
    public boolean isGoing() {
        return mParticipants.contains(ServiceContainer.getSettingsManager().getUserID());
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isNear()
     */
    @Override
    public boolean isNear() {
        Location ourLocation = ServiceContainer.getSettingsManager().getLocation();
        return (ourLocation.distanceTo(mLocation) <= ServiceContainer.getSettingsManager()
            .getNearEventsMaxDistance());
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isOwn()
     */
    @Override
    public boolean isOwn() {
        return this.getCreatorId() == ServiceContainer.getSettingsManager().getUserID();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#isShown()
     */
    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void update(ImmutableEvent event) {
        if ((event.getName() != null) && !event.getName().equals("")) {
            mName = event.getName();
        }

        if (event.getCreatorId() > 0) {
            mCreatorId = event.getCreatorId();
        }

        if ((event.getStartDate() != null) && (event.getEndDate() != null)) {
            mStartDate = new GregorianCalendar(TimeZone.getDefault());
            mEndDate = new GregorianCalendar(TimeZone.getDefault());
        }

        if (event.getLocation() != null) {
            mLocation = new Location(event.getLocation());
        }

        if (event.getLocationString() != null) {
            mLocationString = event.getLocationString();
        }

        if (event.getDescription() == null) {
            mDescription = event.getDescription();
        }
    }
}
