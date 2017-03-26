package org.droidklavier.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

  public static final String HOST = "pref_host";
  public static final String PASS = "pref_pass";
  public static final String DATABASE = "pref_db";
  public static final String DB_USER = "pref_db_user";
  public static final String DB_PASS = "pref_db_pass";

  public static final String HOST_DEFAULT = "192.168.88.1";
  public static final String PASS_DEFAULT = "prc";
  public static final String DATABASE_DEFAULT = "mk4db";
  public static final String DB_USER_DEFAULT = "rcuser";
  public static final String DB_PASS_DEFAULT = "prc100trc100";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // addPreferencesFromResource(R.xml.preferences);
  }
}