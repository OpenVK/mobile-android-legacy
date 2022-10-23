package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;


public class MainActivity extends Activity {

    private Global global = new Global();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        Timer timer = new Timer();
        timer.schedule(new AutoRun(), 1000);
    }

    class AutoRun extends TimerTask {
        public void run() {
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
            if (instance_prefs.getString("server", "").length() == 0 || instance_prefs.getString("access_token", "").length() == 0 || instance_prefs.getString("account_password", "").length() == 0) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, AppActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
