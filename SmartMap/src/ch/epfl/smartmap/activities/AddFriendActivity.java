package ch.epfl.smartmap.activities;

import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.gui.FriendListItemAdapter;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClientException;

/**
 * This Activity displays a list of users from the DB and lets you send them
 * friend requests
 * 
 * @author rbsteinm
 */
public class AddFriendActivity extends ListActivity {

	private SearchView mSearchBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_add_friend);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.add_friend, menu);
		mSearchBar = (SearchView) menu.findItem(
		    R.id.add_friend_activity_searchBar).getActionView();
		this.setSearchBarListener();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.add_friend_activity_searchBar) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
	    long id) {
		long userId = (Long) view.getTag();
		RelativeLayout rl = (RelativeLayout) view;
		TextView tv = (TextView) rl.getChildAt(1);
		assert (tv instanceof TextView)
		    && (tv.getId() == R.id.activity_friends_name);
		String name = tv.getText().toString();
		this.displayConfirmationDialog(name, userId);
	}

	private void setSearchBarListener() {
		mSearchBar.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String newText) {
				new RefreshUserList().execute(newText);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				new RefreshUserList().execute(newText);
				return true;
			}
		});
	}

	private void displayConfirmationDialog(String name, final long userId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Add " + name + " as a friend?");

		// Add positive button
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				new SendFriendRequest().execute(userId);
			}
		});

		// Add negative button
		builder.setNegativeButton("Cancel",
		    new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int id) {
				    dialog.cancel();
			    }
		    });

		// display the AlertDialog
		builder.create().show();
	}

	/**
	 * Asynchronous task that refreshes the list of users displayed every time a
	 * new
	 * character is typed in the searchbar
	 * 
	 * @author rbsteinm
	 */
	private class RefreshUserList extends AsyncTask<String, Void, List<User>> {

		@Override
		protected List<User> doInBackground(String... params) {
			try {
				if (params[0].equals("")) {
					return Collections.emptyList();
				} else {
					return NetworkSmartMapClient.getInstance().findUsers(
					    params[0]);
				}
			} catch (SmartMapClientException e) {
				return Collections.emptyList();
			}
		}

		@Override
		protected void onPostExecute(List<User> refreshedList) {
			super.onPostExecute(refreshedList);
			AddFriendActivity.this.setListAdapter(new FriendListItemAdapter(
			    AddFriendActivity.this, refreshedList));
		}
	}

	/**
	 * Asynchronous task that sends a friend request to the friend whose id is
	 * given in parameter
	 * 
	 * @author rbsteinm
	 */
	private class SendFriendRequest extends AsyncTask<Long, Void, String> {

		@Override
		protected String doInBackground(Long... params) {
			String confirmString = "";
			try {
				NetworkSmartMapClient.getInstance().inviteFriend(params[0]);
				confirmString = "You sent a friend request to "
				    + NetworkSmartMapClient.getInstance()
				        .getUserInfo(params[0]).getName();
			} catch (SmartMapClientException e) {
				// confirmString = "Error, friend request wasn't sent";
				confirmString = e.getMessage();
			}
			return confirmString;
		}

		@Override
		protected void onPostExecute(String confirmString) {
			Toast.makeText(AddFriendActivity.this.getApplicationContext(),
			    confirmString, Toast.LENGTH_LONG).show();
		}
	}
}