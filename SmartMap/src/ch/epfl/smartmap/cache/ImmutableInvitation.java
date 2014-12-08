package ch.epfl.smartmap.cache;

/**
 * Immutable implementation of Invitation, which serves only as container
 * purposes,
 * therefore all set methods are
 * disabled.
 * 
 * @author agpmilli
 */
public final class ImmutableInvitation {

    public static final int FRIEND_INVITATION = 0;
    public static final int EVENT_INVITATION = 1;
    public static final int ACCEPTED_FRIEND_INVITATION = 2;

    // Invitation informations
    private long mUserId;
    private long mEventId;
    private int mStatus;
    private long mTimeStamp;
    private int mType;

    // These will be instanciated and put here by the Cache
    private Event mEvent;
    private User mUser;

    /**
     * Constructor, put {@code null} (or {@code User.NO_ID} for id) if you dont
     * want the value to be taken
     * into account.
     */
    public ImmutableInvitation(long userId, long eventId, int status, long timeStamp, int type) {
        mUserId = userId;
        mEventId = eventId;
        mStatus = status;
        mType = type;
        mTimeStamp = timeStamp;
    }

    public Event getEvent() {
        return mEvent;
    }

    public long getEventId() {
        return mEventId;
    }

    public int getStatus() {
        return mStatus;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public int getType() {
        return mType;
    }

    public User getUser() {
        return mUser;
    }

    public long getUserId() {
        return mUserId;
    }

    public ImmutableInvitation setEvent(Event newEvent) {
        mEvent = newEvent;
        return this;
    }

    public ImmutableInvitation setStatus(int newStatus) {
        mStatus = newStatus;
        return this;
    }

    public ImmutableInvitation setTimeStamp(long newTimeStamp) {
        mTimeStamp = newTimeStamp;
        return this;
    }

    public ImmutableInvitation setType(int newType) {
        mType = newType;
        return this;
    }

    public ImmutableInvitation setUser(User newUser) {
        mUser = newUser;
        return this;
    }
}