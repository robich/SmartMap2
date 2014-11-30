package ch.epfl.smartmap.cache;

import java.util.Calendar;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Represents an online User which is not a Friend, therefore allowing less informations. Every instanc is
 * unique and store in a local cache that can be accessed via static methods on this class.
 * 
 * @author jfperren
 */
public class Stranger extends AbstractUser {

    private long mId;
    private String mName;
    private Bitmap mImage;

    protected Stranger(ImmutableUser user) {
        this.mId = user.getId();
        this.mName = user.getName();
        this.mImage = user.getImage();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getEmail()
     */
    @Override
    public String getEmail() {
        return NO_EMAIL;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getID()
     */
    @Override
    public long getId() {
        return mId;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Displayable#getImage()
     */
    @Override
    public Bitmap getImage() {
        return mImage;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getLastSeen()
     */
    @Override
    public Calendar getLastSeen() {
        return NO_LAST_SEEN;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#getLatLng()
     */
    @Override
    public LatLng getLatLng() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getLocation()
     */
    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getLocationString()
     */
    @Override
    public String getLocationString() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#getMarkerOptions()
     */
    @Override
    public MarkerOptions getMarkerOptions() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getName()
     */
    @Override
    public String getName() {
        return mName;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.User#getNumber()
     */
    @Override
    public String getPhoneNumber() {
        return NO_NUMBER;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Displayable#getSubtitle()
     */
    @Override
    public String getSubtitle() {
        return "Add as Friend to see more informations";
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
     * @see ch.epfl.smartmap.cache.User#getType()
     */
    @Override
    public Type getType() {
        return Type.STRANGER;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.Localisable#isVisible()
     */
    @Override
    public boolean isVisible() {
        return false;
    }

    public void update(ImmutableUser user) {

    }
}
