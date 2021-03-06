package ch.epfl.smartmap.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.ServiceContainer;

import com.facebook.Session;

/**
 * This Activity displays the introduction to the app and the authentication if
 * you are not already logged in, in the
 * other case it just loads mainActivity
 * 
 * @author agpmilli
 * @author SpicyCH
 */
public class StartActivity extends FragmentActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    private ImageView mLogoImage;
    private TextView mWelcomeText;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private com.facebook.widget.LoginButton mLoginButton;

    /**
     * Iniatilizes the user interface.
     */
    private void initializeGUI() {

        // Set background color of activity
        this.setActivityBackgroundColor(this.getResources().getColor(R.color.main_blue));

        // Get all views
        mLogoImage = (ImageView) this.findViewById(R.id.logo);
        mWelcomeText = (TextView) this.findViewById(R.id.welcome);
        mLoginButton = (com.facebook.widget.LoginButton) this.findViewById(R.id.loginButton);
        mProgressBar = (ProgressBar) this.findViewById(R.id.loadingBar);
        mProgressText = (TextView) this.findViewById(R.id.loadingTextView);

        // Not logged in Facebook or permission to use Facebook in SmartMap not
        // given
        if ((Session.getActiveSession() == null) || Session.getActiveSession().getPermissions().isEmpty()) {

            // Start logo and text animation
            mLogoImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.logo_anim));
            mWelcomeText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.welcome_anim));

            // Set facebook button's, progress bar's and progress text's
            // visibility to invisible
            mLoginButton.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressText.setVisibility(View.INVISIBLE);

            // We set a time out to use postDelayed method
            int timeOut = this.getResources().getInteger(R.integer.offset_runnable);

            // Wait for the end of welcome animation before instantiate the
            // facebook fragment and use it
            mWelcomeText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    StartActivity.this.getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, new LoginFragment()).commit();
                }
            }, timeOut);
        } else {
            // Hide all views except progress bar and text
            mLoginButton.setVisibility(View.INVISIBLE);
            mWelcomeText.setVisibility(View.INVISIBLE);
            mLogoImage.setVisibility(View.INVISIBLE);

            this.getSupportFragmentManager().beginTransaction().add(android.R.id.content, new LoginFragment()).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_start);

        ServiceContainer.initSmartMapServices(this);
        this.printSha1InLogcat();
        this.initializeGUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays the facebook app hash in LOG.d. This hash must match the one
     * defined in facebook and google developer.
     * 
     * @author SpicyCH
     */
    private void printSha1InLogcat() {
        try {
            Log.d(TAG, "Retrieving sha1 app hash...");
            PackageInfo info =
                this.getPackageManager().getPackageInfo("ch.epfl.smartmap", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot retrieve the sha1 hash for this app (used by fb)" + e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot retrieve the sha1 hash for this app (used by fb)" + e);
        }
    }

    /**
     * Set background color of activity
     * 
     * @param color
     *            the color
     */
    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }
}
