
package org.scoutant.blokish;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/** Hosts the application preferences screen. */
public class Settings extends PreferenceActivity {
	/**
	 * Replaces the activity content with the preferences fragment.
	 * @param savedInstanceState previously saved activity state, if any
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager()
		.beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();

		getActionBar().setDisplayHomeAsUpEnabled(true);		
	}

	static public class SettingsFragment extends PreferenceFragment {
		/**
		 * Loads preference definitions from the application's preferences resource.
		 * @param savedInstanceState previously saved fragment state, if any
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}	
}
