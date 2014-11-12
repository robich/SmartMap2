package ch.epfl.smartmap.cache;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClientException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.LongSparseArray;

/**
 * SQLite helper
 * @author ritterni
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SmartMapDB";

    public static final String TABLE_USER = "users";
    public static final String TABLE_FILTER = "filters";
    public static final String TABLE_FILTER_USER = "filter_users";
    public static final String TABLE_EVENT = "events";

    private static final String KEY_USER_ID = "userID";
    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_POSNAME = "posName";
    private static final String KEY_ONLINE = "isOnline";
    private static final String KEY_LASTSEEN = "lastSeen";
    private static final String KEY_VISIBLE = "isVisible";

    private static final String KEY_ID = "id";
    private static final String KEY_FILTER_ID = "filterID";

    private static final String KEY_DATE = "date";
    private static final String KEY_ENDDATE = "endDate";

    private static final String KEY_CREATOR_NAME = "creatorName";
    private static final String KEY_EVTDESC = "eventDescription";

    //Columns for the User table
    private static final String[] USER_COLUMNS = {
        KEY_USER_ID, KEY_NAME, KEY_NUMBER, KEY_EMAIL,
        KEY_LONGITUDE, KEY_LATITUDE, KEY_POSNAME, KEY_ONLINE, KEY_LASTSEEN, KEY_VISIBLE
    };

    //Columns for the Filter table
    private static final String[] FILTER_COLUMNS = {
        KEY_ID, KEY_NAME
    };

    //Columns for the Filter/User table
    private static final String[] FILTER_USER_COLUMNS = {
        KEY_ID, KEY_FILTER_ID, KEY_USER_ID
    };

    //Columns for the Event table
    private static final String[] EVENT_COLUMNS = {
        KEY_ID, KEY_NAME, KEY_EVTDESC, KEY_USER_ID, KEY_CREATOR_NAME,
        KEY_LONGITUDE, KEY_LATITUDE, KEY_POSNAME, KEY_DATE, KEY_ENDDATE
    };

    //Table of users
    private static final String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USER + "("
            + KEY_USER_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_NUMBER + " TEXT,"
            + KEY_EMAIL + " TEXT,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_POSNAME + " TEXT,"
            + KEY_ONLINE + " INTEGER,"
            + KEY_LASTSEEN + " INTEGER,"
            + KEY_VISIBLE + " INTEGER"
            + ")";

    //Table of filters
    private static final String CREATE_TABLE_FILTER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_FILTER + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT"
            + ")";

    //Table that maps filters to users
    private static final String CREATE_TABLE_FILTER_USER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_FILTER_USER + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_FILTER_ID + " INTEGER,"
            + KEY_USER_ID + " INTEGER"
            + ")";

    //Table of events
    private static final String CREATE_TABLE_EVENT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_EVENT + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_EVTDESC + " TEXT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_CREATOR_NAME + " TEXT,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_POSNAME + " TEXT,"
            + KEY_DATE + " INTEGER,"
            + KEY_ENDDATE + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_FILTER);
        db.execSQL(CREATE_TABLE_FILTER_USER);
        db.execSQL(CREATE_TABLE_EVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTER_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);

        onCreate(db);
    }

    /**
     * Clears the database. Mainly for testing purposes.
     */
    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db, 1, 2);
    }

    /**
     * Adds a user to the internal database. If an user with the same ID already exists, updates that user instead.
     * @param user The user to add to the database
     */
    public void addUser(User user) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(
                TABLE_USER,
                USER_COLUMNS,
                KEY_USER_ID + " = ?",
                new String[] {String.valueOf(user.getID())},
                null,
                null,
                null,
                null);

        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, user.getID());
            values.put(KEY_NAME, user.getName());
            values.put(KEY_NUMBER, user.getNumber());
            values.put(KEY_EMAIL, user.getEmail());
            values.put(KEY_LONGITUDE, user.getLocation().getLongitude());
            values.put(KEY_LATITUDE, user.getLocation().getLatitude());
            values.put(KEY_POSNAME, user.getPositionName());
            values.put(KEY_ONLINE, user.isOnline() ? 1 : 0); //boolean to int
            values.put(KEY_LASTSEEN, user.getLastSeen().getTimeInMillis());
            values.put(KEY_VISIBLE, user.isVisible() ? 1 : 0); //boolean to int

            db.insert(TABLE_USER, null, values);

            db.close();
        } else {
            db.close();
            updateUser(user);
        }

        cursor.close();
    }

    /**
     * Gets a user from the database
     * @param id The user's unique ID
     * @return The user as a Friend object
     */
    public User getUser(long id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USER,
                USER_COLUMNS,
                KEY_USER_ID + " = ?",
                new String[] {String.valueOf(id)},
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();
        }


        Friend friend = new Friend(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_NAME)));
        friend.setNumber(cursor.getString(cursor.getColumnIndex(KEY_NUMBER)));
        friend.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
        friend.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
        friend.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));
        friend.setPositionName(cursor.getString(cursor.getColumnIndex(KEY_POSNAME)));
        friend.setOnline(cursor.getInt(cursor.getColumnIndex(KEY_ONLINE)) == 1); //int to boolean
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_LASTSEEN)));
        friend.setLastSeen(cal);
        friend.setVisible(cursor.getInt(cursor.getColumnIndex(KEY_VISIBLE)) == 1); //int to boolean

        cursor.close();

        return friend;
    }

    /**
     * Creates a LongSparseArray containing all users in the database, mapped to their ID
     * @return A LongSparseArray of all users
     */
    public LongSparseArray<User> getAllUsers() {
        LongSparseArray<User> friends = new LongSparseArray<User>();

        String query = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Friend friend = null;
        if (cursor.moveToFirst()) {
            do {
                friend = new Friend(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                friend.setNumber(cursor.getString(cursor.getColumnIndex(KEY_NUMBER)));
                friend.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                friend.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
                friend.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));
                friend.setPositionName(cursor.getString(cursor.getColumnIndex(KEY_POSNAME)));
                friend.setOnline(cursor.getInt(cursor.getColumnIndex(KEY_ONLINE)) == 1); //int to boolean
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_LASTSEEN)));
                friend.setLastSeen(cal);
                friend.setVisible(cursor.getInt(cursor.getColumnIndex(KEY_VISIBLE)) == 1); //int to boolean

                friends.put(friend.getID(), friend);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return friends;
    }

    /**
     * Updates a user's values
     * @param user The user to update
     * @return The number of rows that were updated
     */
    public int updateUser(User user) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, user.getID());
        values.put(KEY_NAME, user.getName());
        values.put(KEY_NUMBER, user.getNumber());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_LONGITUDE, user.getLocation().getLongitude());
        values.put(KEY_LATITUDE, user.getLocation().getLatitude());
        values.put(KEY_POSNAME, user.getPositionName());
        values.put(KEY_ONLINE, user.isOnline() ? 1 : 0); //boolean to int
        values.put(KEY_LASTSEEN, user.getLastSeen().getTimeInMillis());
        values.put(KEY_VISIBLE, user.isVisible() ? 1 : 0); //boolean to int

        int rows = db.update(TABLE_USER,
                values,
                KEY_USER_ID + " = ?",
                new String[] {String.valueOf(user.getID())});

        db.close();

        return rows;
    }

    /**
     * Deletes a user from the database
     * @param id The user's id
     */
    public void deleteUser(long id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER,
                KEY_USER_ID + " = ?",
                new String[] {String.valueOf(id)});

        db.close();
    }

    /**
     * Adds a filter/userlist/friendlist to the database, and gives an ID to the filter
     * @param filter The filter/list to add
     * @return The ID of the newly added filter in the filter database
     */
    public long addFilter(UserList filter) {

        SQLiteDatabase db = this.getWritableDatabase();

        //First we insert the filter in the table of lists
        ContentValues filterValues = new ContentValues();
        filterValues.put(KEY_NAME, filter.getListName());
        long filterID = db.insert(TABLE_FILTER, null, filterValues);

        //Then we add the filter-user pairs to another table
        ContentValues pairValues = null;
        for (long id : filter.getList()) {
            pairValues = new ContentValues();
            pairValues.put(KEY_FILTER_ID, filterID);
            pairValues.put(KEY_USER_ID, id);
            db.insert(TABLE_FILTER_USER, null, pairValues);
        }
        db.close();
        filter.setID(filterID); //sets an ID so the filter can be easily accessed
        return filterID;
    }

    /**
     * Gets a specific filter by its id
     * @param name The filter's id
     * @return The filter as a FriendList object
     */
    public UserList getFilter(long id) {

        SQLiteDatabase db = this.getWritableDatabase();

        //First query to get the filter's name
        Cursor cursor = db.query(
                TABLE_FILTER,
                FILTER_COLUMNS,
                KEY_ID + " = ?",
                new String[] {String.valueOf(id)},
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        FriendList filter = new FriendList(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
        filter.setID(id);

        //Second query to get the associated list of IDs
        cursor = db.query(
                TABLE_FILTER_USER,
                FILTER_USER_COLUMNS,
                KEY_FILTER_ID + " = ?",
                new String[] {String.valueOf(id)},
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            do {
                filter.addUser(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return filter;
    }

    /**
     * Gets all the filters from the database
     * @return A list of FriendLists
     */
    public List<UserList> getAllFilters() {

        ArrayList<UserList> filters = new ArrayList<UserList>();

        String query = "SELECT  * FROM " + TABLE_FILTER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //using getFilter to add this row's filter to the list
                filters.add(getFilter(cursor.getLong(cursor.getColumnIndex(KEY_ID))));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return filters;
    }

    /**
     * Deletes a filter from the database
     * @param filter The filter to delete
     */
    public void deleteFilter(long id) {

        SQLiteDatabase db = this.getWritableDatabase();

        //delete the filter from the table of filters
        db.delete(TABLE_FILTER,
                KEY_ID + " = ?",
                new String[] {String.valueOf(id)});

        //then delete all the rows that reference this filter in the filter-user table
        db.delete(TABLE_FILTER_USER,
                KEY_FILTER_ID + " = ?",
                new String[] {String.valueOf(id)});

        db.close();
    }

    /**
     * Updates a filter
     * @param filter The updated filter
     */
    public void updateFilter(UserList filter) {

        deleteFilter(filter.getID());
        addFilter(filter);
        //not sure if there's a more efficient way
    }

    /**
     * Stores an event in the database. If there's already an event with the same ID, updates that event instead
     * The event must have an ID (given by the server)!
     * @param event The event to store
     */
    public void addEvent(Event event) throws IllegalArgumentException {
    	if (event.getID() < 0) {
    		throw new IllegalArgumentException("Invalid event ID");
    	}

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(
                TABLE_EVENT,
                EVENT_COLUMNS,
                KEY_ID + " = ?",
                new String[] {String.valueOf(event.getID())},
                null,
                null,
                null,
                null);

        //We check if the even is already there
        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(KEY_ID, event.getID());
            values.put(KEY_NAME, event.getName());
            values.put(KEY_EVTDESC, event.getDescription());
            values.put(KEY_USER_ID, event.getCreator());
            values.put(KEY_CREATOR_NAME, event.getCreatorName());
            values.put(KEY_LONGITUDE, event.getLocation().getLongitude());
            values.put(KEY_LATITUDE, event.getLocation().getLatitude());
            values.put(KEY_POSNAME, event.getPositionName());
            values.put(KEY_DATE, event.getStartDate().getTimeInMillis());
            values.put(KEY_ENDDATE, event.getEndDate().getTimeInMillis());

            db.insert(TABLE_EVENT, null, values);

            db.close();
        } else {
            db.close();
            updateEvent(event);
        }

        cursor.close();
    }

    /**
     * @param id The event's ID
     * @return The event associated to this ID
     */
    public Event getEvent(long id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EVENT,
                EVENT_COLUMNS,
                KEY_ID + " = ?",
                new String[] {String.valueOf(id)},
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        GregorianCalendar startDate = new GregorianCalendar();
        GregorianCalendar endDate = new GregorianCalendar();

        startDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_DATE)));
        endDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_ENDDATE)));

        Location loc = new Location("");
        loc.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
        loc.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));

        UserEvent event = new UserEvent(cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_CREATOR_NAME)),
                startDate,
                endDate,
                loc);

        event.setID(id);
        event.setDescription(cursor.getString(cursor.getColumnIndex(KEY_EVTDESC)));
        event.setPositionName(cursor.getString(cursor.getColumnIndex(KEY_POSNAME)));

        cursor.close();

        return event;
    }

    /**
     * @return The list of all events
     */
    public List<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<Event>();

        String query = "SELECT  * FROM " + TABLE_EVENT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
            	events.add(getEvent(cursor.getLong(cursor.getColumnIndex(KEY_ID))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    /**
     * Deletes an event from the database
     * @param event The event to delete
     */
    public void deleteEvent(long id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_EVENT,
                KEY_ID + " = ?",
                new String[] {String.valueOf(id)});

        db.close();
    }

    /**
     * Updates an event
     * @param event The event to update
     * @return The number of rows that were affected
     */
    public int updateEvent(Event event) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, event.getID());
        values.put(KEY_NAME, event.getName());
        values.put(KEY_EVTDESC, event.getDescription());
        values.put(KEY_USER_ID, event.getCreator());
        values.put(KEY_CREATOR_NAME, event.getCreatorName());
        values.put(KEY_LONGITUDE, event.getLocation().getLongitude());
        values.put(KEY_LATITUDE, event.getLocation().getLatitude());
        values.put(KEY_POSNAME, event.getPositionName());
        values.put(KEY_DATE, event.getStartDate().getTimeInMillis());
        values.put(KEY_ENDDATE, event.getEndDate().getTimeInMillis());

        int rows = db.update(TABLE_EVENT,
                values,
                KEY_ID + " = ?",
                new String[] {String.valueOf(event.getID())});

        db.close();

        return rows;
    }
    
    /**
     * Uses listFriendsPos() to update the entire friends database with updated positions
     * @return The number of rows (i.e. friends) that were updated
     */
    public int refreshFriendsPos() {
        int updatedFriends = 0;
        try {
            Map<Long, Location> locations = NetworkSmartMapClient.getInstance().listFriendsPos();
            Set<Long> keySet = locations.keySet(); //the keys are friend IDs
            User friend;
            for (long i : keySet) {
                friend = getUser(i);
                friend.setLocation(locations.get(i));
                updatedFriends += updateUser(friend);
            }
        } catch (SmartMapClientException e) {
            e.printStackTrace();
        }
        return updatedFriends;
    }
    
    /**
     * Fully updates the friends database (not only positions)
     */
    public void refreshFriendsInfo() {
        LongSparseArray<User> friends = getAllUsers();
        NetworkSmartMapClient client = NetworkSmartMapClient.getInstance();
        for (int i = 0; i < friends.size(); i++) {
            try {
                //The keys are IDs, so we call getUserInfo for each key and update the db with the result
                updateUser(client.getUserInfo(friends.keyAt(i)));
            } catch (SmartMapClientException e) {
                e.printStackTrace();
            }
        }
    }
}
