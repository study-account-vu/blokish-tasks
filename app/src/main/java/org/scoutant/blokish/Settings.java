
package org.scoutant.blokish;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceActivity {
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
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}	
}
