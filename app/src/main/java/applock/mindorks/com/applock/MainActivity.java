package applock.mindorks.com.applock;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

import applock.mindorks.com.applock.Data.AppInfo;
import applock.mindorks.com.applock.Fragments.AllAppFragment;
import applock.mindorks.com.applock.Fragments.PasswordFragment;
import applock.mindorks.com.applock.Utils.AppLockConstants;
import applock.mindorks.com.applock.Utils.AppLockLogEvents;


public class MainActivity extends AppCompatActivity {

    //save our header or result
    private Drawer.Result result = null;
    FragmentManager fragmentManager;
    Context context;
    Dialog dialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    long numOfTimesAppOpened = 0;
    boolean isRated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences(AppLockConstants.MyPREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        numOfTimesAppOpened = sharedPreferences.getLong(AppLockConstants.NUM_OF_TIMES_APP_OPENED, 0) + 1;
        isRated = sharedPreferences.getBoolean(AppLockConstants.IS_RATED, false);
        editor.putLong(AppLockConstants.NUM_OF_TIMES_APP_OPENED, numOfTimesAppOpened);
        editor.commit();
        //Google Analytics
        Tracker t = ((AppLockApplication) getApplication()).getTracker(AppLockApplication.TrackerName.APP_TRACKER);
        t.setScreenName(AppLockConstants.MAIN_SCREEN);
        t.send(new HitBuilders.AppViewBuilder().build());
        if (Build.VERSION.SDK_INT > 20) {
            Toast.makeText(getApplicationContext(), "If you have not allowed , allow App Lock so that it can work properly from sliding menu options", Toast.LENGTH_LONG).show();
        }

        fragmentManager = getSupportFragmentManager();

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Create the drawer
        result = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("All Applications").withIcon(FontAwesome.Icon.faw_home),
                        new PrimaryDrawerItem().withName("Locked Applications").withIcon(FontAwesome.Icon.faw_lock),
                        new PrimaryDrawerItem().withName("Unlocked Applications").withIcon(FontAwesome.Icon.faw_unlock),
                        new PrimaryDrawerItem().withName("Change Password").withIcon(FontAwesome.Icon.faw_exchange),
                        new PrimaryDrawerItem().withName("Allow Access").withIcon(FontAwesome.Icon.faw_share)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable) {

                            if (position == 0) {
                                getSupportActionBar().setTitle("All Applications");
                                Fragment f = AllAppFragment.newInstance(AppLockConstants.ALL_APPS);
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit();
                                AppLockLogEvents.logEvents(AppLockConstants.MAIN_SCREEN, "Show All Applications Clicked", "show_all_applications_clicked", "");
                            }

                            if (position == 1) {
                                getSupportActionBar().setTitle("Locked Applications");
                                Fragment f = AllAppFragment.newInstance(AppLockConstants.LOCKED);
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit();
                                AppLockLogEvents.logEvents(AppLockConstants.MAIN_SCREEN, "Show Locked Applications Clicked", "show_locked_applications_clicked", "");
                            }

                            if (position == 2) {
                                getSupportActionBar().setTitle("Unlocked Applications");
                                Fragment f = AllAppFragment.newInstance(AppLockConstants.UNLOCKED);
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit();
                                AppLockLogEvents.logEvents(AppLockConstants.MAIN_SCREEN, "Show Unlocked Applications Clicked", "show_unLocked_applications_clicked", "");
                            }

                            if (position == 3) {
                                getSupportActionBar().setTitle("Change Password");
                                Fragment f = PasswordFragment.newInstance();
                                fragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit();
                                AppLockLogEvents.logEvents(AppLockConstants.MAIN_SCREEN, "Password Changed Clicked", "password_changed_clicked", "");
                            }

                            if (position == 4) {
                                final Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), "If you have not allowed , allow App Lock so that it can work properly", Toast.LENGTH_LONG).show();
                                AppLockLogEvents.logEvents(AppLockConstants.MAIN_SCREEN, "Allow Access", "allow_access", "");
                                result.setSelection(0);
                            }

                        }
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        KeyboardUtil.hideKeyboard(MainActivity.this);
                    }


                    @Override
                    public void onDrawerClosed(View drawerView) {


                    }
                })
                .withFireOnInitialOnClick(true)
                .withSavedInstance(savedInstanceState)
                .build();
        result.keyboardSupportEnabled(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            if (getCurrentFragment() instanceof AllAppFragment) {
                super.onBackPressed();
            } else {
                fragmentManager.popBackStack();
                getSupportActionBar().setTitle("AllAppFragment");
                Fragment f = AllAppFragment.newInstance(AppLockConstants.ALL_APPS);
                fragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit();
                result.setSelection(0);
            }
        }
    }

    /**
     * Returns currentfragment
     *
     * @return
     */
    public Fragment getCurrentFragment() {
        // TODO Auto-generated method stub
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
//            startActivity(i);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    /**
     * get the list of all installed applications in the device
     *
     * @return ArrayList of installed applications or null
     */
    public static List<AppInfo> getListOfInstalledApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<AppInfo> installedApps = new ArrayList();
        List<PackageInfo> apps = packageManager.getInstalledPackages(PackageManager.SIGNATURE_MATCH);
        if (apps != null && !apps.isEmpty()) {

            for (int i = 0; i < apps.size(); i++) {
                PackageInfo p = apps.get(i);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = packageManager.getApplicationInfo(p.packageName, 0);
                    AppInfo app = new AppInfo();
                    app.setName(p.applicationInfo.loadLabel(packageManager).toString());
                    app.setPackageName(p.packageName);
                    app.setVersionName(p.versionName);
                    app.setVersionCode(p.versionCode);
                    app.setIcon(p.applicationInfo.loadIcon(packageManager));
                    installedApps.add(app);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return installedApps;
        }
        return null;
    }

    @Override
    protected void onStart() {
        GoogleAnalytics.getInstance(context).reportActivityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        GoogleAnalytics.getInstance(context).reportActivityStop(this);
        super.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

}
