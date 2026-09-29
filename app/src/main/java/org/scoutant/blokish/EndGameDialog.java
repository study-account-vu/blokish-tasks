package org.scoutant.blokish;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/** Presents the final game message and optional winner artwork. */
public class EndGameDialog extends Dialog {
	
	/**
	 * Builds the end-of-game dialog.
	 *
	 * @param context context hosting the dialog
	 * @param redwins whether the human-controlled red side won
	 * @param message localized result text to display
	 * @param level requested AI level associated with the completed game
	 * @param score winning score
	 */
	public EndGameDialog(final Context context, boolean redwins, String message, final int level, final int score) {
		super(context);
		setContentView( R.layout.endgame);
		getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView tv = (TextView) findViewById(R.id.message);
		tv.setText( message);
		View b = findViewById(R.id.ok);
		b.setOnClickListener( new android.view.View.OnClickListener(){
			/**
			 * Dismisses the result dialog after acknowledgement.
			 * @param v dialog control receiving the click
			 */
			public void onClick(View v) {
				EndGameDialog.this.dismiss();
			}
		});
		findViewById(R.id.icons).setVisibility( redwins ? View.VISIBLE : View.GONE);
	}
}
