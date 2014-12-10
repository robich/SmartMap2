package ch.epfl.smartmap.cache;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.ServiceContainer;
import ch.epfl.smartmap.util.Utils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

/**
 * An event that can be seen on the map
 * 
 * @author ritterni
 * @author SpicyCH (PublicEvents are now Parcelable)
 */

public class PublicEvent implements Event {

    static final public String TAG = PublicEvent.class.getSimpleName();

    // Mandatory fields
    private final long mId;
    private String mName;
    private Set<Long> mParticipantIds;
    private Calendar mStartDate;
    private Calendar mEndDate;
    private Location mLocation;
    // Optional fields
    private String mDescription;
    private String mLocationString;

    private final UserInterface mCreator;

    public static final int DEFAULT_ICON = R.drawable.default_event;

    public static final float MARKER_ANCHOR_X = (float) 0.5;
    public static final float MARKER_ANCHOR_Y = 1;

    protected PublicEvent(ImmutableEvent event) {
        // Set id
        if (event.getId() >= 0) {
            mId = event.getId();
        } else {
            throw new IllegalArgumentException("Trying to create an Event with incorrect Id");
        }

        // Set other values
        mName = (event.getName() != null) ? event.getName() : NO_NAME;
        mCreator = (event.getCreator() != null) ? event.getCreator() : UserInterface.NOBODY;
        mStartDate = (event.getStartDate() != null) ? (Calendar) event.getStartDate().clone() : NO_START_DATE;
        mEndDate = (event.getEndDate() != null) ? (Calendar) event.getEndDate().clone() : NO_END_DATE;
        mLocation = (event.getLocation() != null) ? new Location(event.getLocation()) : NO_LOCATION;
        mLocationString =
            (event.getLocationString() != null) ? event.getLocationString() : NO_LOCATION_STRING;
        mDescription = (event.getDescription() != null) ? event.getDescription() : NO_DESCRIPTION;
        mParticipantIds =
            (event.getParticipantIds() != null) ? new HashSet<Long>(event.getParticipantIds())
                : NO_PARTICIPANTIDS;
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

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#getCreator()
     */
    @Override
    public UserInterface getCreator() {
        return mCreator;
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
        return new ImmutableEvent(mId, mName, mCreator.getImmutableCopy(), mDescription, mStartDate,
            mEndDate, mLocation, mLocationString, mParticipantIds);
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

    @Override
    public BitmapDescriptor getMarkerIcon(Context context) {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
    }

    @Override
    public String getName() {
        return mName;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#getParticipants()
     */
    @Override
    public Set<Long> getParticipantIds() {
        return mParticipantIds;
    }

    @Override
    public Calendar getStartDate() {
        return mStartDate;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Displayable#getSubtitle()
     */
    @Override
    public String getSubtitle() {
        return Utils.getDateString(mStartDate) + " at " + Utils.getTimeString(mStartDate) + ", near "
            + Utils.getCityFromLocation(mLocation);
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
        return mParticipantIds.contains(ServiceContainer.getSettingsManager().getUserId());
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isLive()
     */
    @Override
    public boolean isLive() {
        Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+01:00"));
        // Maybe it is the other way around
        return (mStartDate.compareTo(now) <= 0) && (mEndDate.compareTo(now) >= 0);
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isNear()
     */
    @Override
    public boolean isNear() {
        Location ourLocation = ServiceContainer.getSettingsManager().getLocation();
        return ourLocation.distanceTo(mLocation) <= ServiceContainer.getSettingsManager()
            .getNearEventsMaxDistance();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Event#isOwn()
     */
    @Override
    public boolean isOwn() {
        Log.d(TAG, "creator id : " + mCreator.getId() + "  my id : "
            + ServiceContainer.getSettingsManager().getUserId());
        return mCreator.getId() == ServiceContainer.getSettingsManager().getUserId();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#isShown()
     */
    @Override
    public boolean isVisible() {
        // TODO Implement Settings here
        return true;
    }

    @Override
    public void update(ImmutableEvent event) {
        if ((event.getName() != null) && !event.getName().equals("")) {
            mName = event.getName();
        }

        if ((event.getStartDate() != null) && (event.getEndDate() != null)) {
            mStartDate = (Calendar) event.getStartDate().clone();
            mEndDate = (Calendar) event.getEndDate().clone();
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

        if (event.getParticipantIds() != null) {
            mParticipantIds = event.getParticipantIds();
        }
    }
}
