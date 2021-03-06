package ch.epfl.smartmap.test.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.test.AndroidTestCase;
import android.util.Log;
import ch.epfl.smartmap.background.ServiceContainer;
import ch.epfl.smartmap.background.SettingsManager;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.cache.Event;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.callbacks.SearchRequestCallback;
import ch.epfl.smartmap.database.DatabaseHelper;
import ch.epfl.smartmap.search.CachedSearchEngine;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClient;
import ch.epfl.smartmap.test.database.MockContainers;
import ch.epfl.smartmap.test.database.MockInstances;

/**
 * @author ritterni
 */
public class CachedSearchEngineTest extends AndroidTestCase {

    private SmartMapClient client = null;
    private Cache cache = null;
    private DatabaseHelper dbh = null;
    private SettingsManager settings = null;
    private CachedSearchEngine searchEngine;
    private User alain = null;
    private User robin = null;
    private Event polylan = null;
    private Event football = null;
    private Event event = null;
    private User user = null;
    private Set<User> users = null;
    private Set<Event> events = null;

    @Override
    @Before
    public void setUp() throws Exception {

        ServiceContainer.forceInitSmartMapServices(this.getContext());
        searchEngine = new CachedSearchEngine();
        settings = Mockito.mock(SettingsManager.class);
        Mockito.when(settings.getContext()).thenReturn(this.getContext());
        Mockito.when(settings.getUserId()).thenReturn((long) 1);
        ServiceContainer.setSettingsManager(settings);

        client = Mockito.mock(NetworkSmartMapClient.class);
        Mockito.doReturn(Arrays.asList(MockContainers.ALAIN_CONTAINER)).when(client).findUsers("al");
        Mockito.doReturn(User.NO_IMAGE).when(client).getProfilePicture(Mockito.anyLong());
        Mockito.doReturn(MockContainers.ALAIN_CONTAINER).when(client).getUserInfo(MockContainers.ALAIN_ID);
        Mockito.doReturn(MockContainers.ROBIN_CONTAINER).when(client).getUserInfo(MockContainers.ROBIN_ID);
        Mockito.doReturn(MockContainers.JULIEN_CONTAINER).when(client).getUserInfo(MockContainers.JULIEN_ID);
        ServiceContainer.setNetworkClient(client);

        cache = Mockito.mock(Cache.class);

        football = MockInstances.FOOTBALL_TOURNAMENT;
        polylan = MockInstances.POLYLAN;
        robin = MockInstances.ROBIN;
        alain = MockInstances.ALAIN;

        Mockito.when(cache.getEvent(polylan.getId())).thenReturn(polylan);
        Mockito.when(cache.getEvent(football.getId())).thenReturn(football);

        Mockito.when(cache.getUser(MockContainers.ALAIN_CONTAINER.getId())).thenReturn(alain);
        Mockito.when(cache.getUser(MockContainers.ROBIN_CONTAINER.getId())).thenReturn(robin);
        Mockito.when(cache.getAllEvents()).thenReturn(new HashSet<Event>(Arrays.asList(football, polylan)));

        ServiceContainer.setCache(cache);

        dbh = Mockito.mock(DatabaseHelper.class);
        Mockito.when(dbh.getEvent(polylan.getId())).thenReturn(MockContainers.POLYLAN_CONTAINER);
        ServiceContainer.setDatabaseHelper(dbh);
    }

    @After
    @Override
    public void tearDown() {
        MockContainers.ALAIN_CONTAINER.setId(MockContainers.ALAIN_ID);
        MockContainers.ALAIN_CONTAINER.setFriendship(MockContainers.ALAIN_FRIENDSHIP);
    }

    @Test
    public void testFindEventById() {
        searchEngine.findEventById(polylan.getId(), new SearchRequestCallback<Event>() {

            @Override
            public void onNetworkError(Exception e) {
                fail("Shouldn't happen");
            }

            @Override
            public void onNotFound() {
                fail("Shouldn't happen");
            }

            @Override
            public void onResult(Event result) {
                event = result;
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(CachedSearchEngineTest.class.getSimpleName(), "Error: " + e);
        }
        assertTrue(polylan.getName().equals(event.getName()));
    }

    @Test
    public void testFindEventsByIds() {
        searchEngine.findEventsByIds(new HashSet<Long>(Arrays.asList(polylan.getId(), football.getId())),
            new SearchRequestCallback<Set<Event>>() {

                @Override
                public void onNetworkError(Exception e) {
                    fail("Shouldn't happen");
                }

                @Override
                public void onNotFound() {
                    fail("Shouldn't happen");
                }

                @Override
                public void onResult(Set<Event> result) {
                    events = result;
                }
            });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(CachedSearchEngineTest.class.getSimpleName(), "Error: " + e);
        }
        boolean gotPolylan = false;
        boolean gotFootball = false;
        for (Event evt : events) {
            gotPolylan = (evt.getName().equals(polylan.getName()));
            gotFootball = (evt.getName().equals(football.getName()));
        }
        assertTrue(gotPolylan);
        assertTrue(gotFootball);
    }

    @Test
    public void testFindStrangersByName() {
        ServiceContainer.setCache(new Cache());
        searchEngine.findStrangersByName("al", new SearchRequestCallback<Set<User>>() {

            @Override
            public void onNetworkError(Exception e) {
                fail("Shouldn't happen");
            }

            @Override
            public void onNotFound() {
                fail("Shouldn't happen");
            }

            @Override
            public void onResult(Set<User> result) {
                user = result.iterator().next();
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(CachedSearchEngineTest.class.getSimpleName(), "Error: " + e);
        }
        assertTrue(user.getId() == alain.getId());
    }

    @Test
    public void testFindUserById() {
        searchEngine.findUserById(alain.getId(), new SearchRequestCallback<User>() {

            @Override
            public void onNetworkError(Exception e) {
                fail("Shouldn't happen");
            }

            @Override
            public void onNotFound() {
                fail("Shouldn't happen");
            }

            @Override
            public void onResult(User result) {
                user = result;
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(CachedSearchEngineTest.class.getSimpleName(), "Error: " + e);
        }
        assertTrue(alain.getName().equals(user.getName()));
    }

    @Test
    public void testFindUsersByIds() {
        searchEngine.findUsersByIds(new HashSet<Long>(Arrays.asList(alain.getId())),
            new SearchRequestCallback<Set<User>>() {

                @Override
                public void onNetworkError(Exception e) {
                    fail("Shouldn't happen");
                }

                @Override
                public void onNotFound() {
                    fail("Shouldn't happen");
                }

                @Override
                public void onResult(Set<User> result) {
                    users = result;
                }
            });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(CachedSearchEngineTest.class.getSimpleName(), "Error: " + e);
        }
        boolean gotAlain = false;
        for (User user : users) {
            gotAlain = (user.getName().equals(alain.getName()));
        }
        assertTrue(gotAlain);
    }
}