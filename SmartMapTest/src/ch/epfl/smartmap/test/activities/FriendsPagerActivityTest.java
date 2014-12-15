package ch.epfl.smartmap.test.activities;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeLeft;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.mockito.Mockito;

import android.test.ActivityInstrumentationTestCase2;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.activities.FriendsPagerActivity;
import ch.epfl.smartmap.background.ServiceContainer;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.cache.Filter;
import ch.epfl.smartmap.cache.Invitation;
import ch.epfl.smartmap.cache.MockGenerator;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.cache.User.BlockStatus;
import ch.epfl.smartmap.cache.UserContainer;
import ch.epfl.smartmap.servercom.SmartMapClientException;
import ch.epfl.smartmap.test.database.MockContainers;

public class FriendsPagerActivityTest extends
ActivityInstrumentationTestCase2<FriendsPagerActivity> {

	User friend;
	Set<User> friendSet;
	Invitation invitation;
	SortedSet<Invitation> invitationSet;

	public FriendsPagerActivityTest() {
		super(FriendsPagerActivity.class);
	}

	// The standard JUnit 3 setUp method run for for every test
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.getActivity();
		this.initializeMockFriendsAndInvitations();
		this.createMockCache();

	}

	public void testClickOnFriendLeadsToFriendsInfo() {
		swipeRight();
		onData(anything()).inAdapterView(withContentDescription("Friend list"))
		.atPosition(0).perform(click());
		onView(withId(R.id.user_info_remove_button)).check(
				matches(isDisplayed()));
		pressBack();
	}

	public void testAOpenAddFriendActivity() {
		onView(withId(R.id.activity_friends_add_button)).perform(click());
		onView(withId(R.id.add_friend_activity_searchBar)).check(
				matches(isDisplayed()));
		pressBack();
	}

	public void testSwipeLeftAndRight() {
		onView(withId(R.id.myViewPager)).perform(swipeLeft());
		onView(withId(R.id.layout_invitations_tab)).check(
				matches(isDisplayed()));
		onView(withId(R.id.myViewPager)).perform(swipeRight());
		onView(withId(R.id.layout_friends_tab)).check(matches(isDisplayed()));
	}

	public void testFriendsNamesAreDisplayed() {
		onView(withText(friend.getName())).check(matches(isDisplayed()));
	}

	public void testFriendsSubtitlesAreDisplayed() {
		onView(withText(friend.getSubtitle())).check(matches(isDisplayed()));
	}

	public void testFriendsProfilePicturesAreDisplayed() {
		onView(withId(R.id.activity_friends_picture)).check(
				matches(isDisplayed()));
	}

	public void testInvitingUsersAreDisplayed() {
		onView(withId(R.id.myViewPager)).perform(swipeLeft());
		onView(withText(invitation.getUser().getName())).check(
				matches(isDisplayed()));
		onView(withId(R.id.myViewPager)).perform(swipeRight());
	}

	public void testInvitingUsersPicturesAreDisplayed() {
		onView(withId(R.id.myViewPager)).perform(swipeLeft());
		onView(withId(R.id.activity_friends_inviter_picture)).check(
				matches(isDisplayed()));
		onView(withId(R.id.myViewPager)).perform(swipeRight());
	}

	// FIXME : cannot click on invitation in invitation list whereas can click
	// on friend in friend list by doing the same way
	public void ignoredtestClickOnInvitingUsersOpensDialog()
			throws InterruptedException {
		onView(withId(R.id.myViewPager)).perform(swipeLeft());
		onData(anything())
		.inAdapterView(withContentDescription("Friend invitation list"))
		.atPosition(0).perform(click());
		onView(withText("Accept")).check(matches(isDisplayed()));
		onView(withId(R.id.myViewPager)).perform(swipeRight());
	}

	private void initializeMockFriendsAndInvitations() {
		friend = User.createFromContainer(new UserContainer(2, "Bob",
				User.NO_PHONE_NUMBER, User.NO_EMAIL, User.NO_LOCATION,
				User.NO_LOCATION_STRING, User.NO_IMAGE, BlockStatus.UNBLOCKED,
				User.FRIEND));
		invitation = MockGenerator.getInvitation(Invitation.FRIEND_INVITATION);

		friendSet = new HashSet<User>();
		invitationSet = new TreeSet<Invitation>();
		friendSet.add(friend);
		invitationSet.add(invitation);
	}

	private void createMockCache() throws SmartMapClientException {
		Cache newCache = Mockito.mock(Cache.class);
		Mockito.when(newCache.getAllFriends()).thenReturn(friendSet);
		Mockito.when(newCache.getUnansweredFriendInvitations()).thenReturn(
				invitationSet);
		Mockito.when(newCache.getUser(2)).thenReturn(friend);
		Mockito.when(newCache.getDefaultFilter()).thenReturn(
				Filter.createFromContainer(MockContainers.FAMILY));
		ServiceContainer.setCache(newCache);
	}

}