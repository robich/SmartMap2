package ch.epfl.smartmap.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.gui.Utils;

/**
 * Describes an event
 * 
 * @author jfperren
 * @author ritterni
 */

public interface Event extends Displayable {

    List<Long> NO_PARTICIPANTS = new ArrayList<Long>();
    String NO_DESCRIPTION = "This event currently has no description";

    Bitmap DEFAULT_IMAGE = BitmapFactory.decodeResource(Utils.sContext.getResources(),
        R.drawable.default_event);

    /**
     * @return The ID of the user who created the event
     */
    long getCreatorId();

    /**
     * @return The event's description
     */
    String getDescription();

    /**
     * @return The date (year, month, day, hour, minute) at which the event ends
     */
    Calendar getEndDate();

    ImmutableEvent getImmutableCopy();

    String getName();

    List<Long> getParticipants();

    /**
     * @return The date (year, month, day, hour, minute) at which the event
     *         starts
     */
    Calendar getStartDate();

    boolean isGoing();

    boolean isNear();

    boolean isOwn();

    void update(ImmutableEvent event);
}
