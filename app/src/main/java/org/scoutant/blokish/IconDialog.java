package org.scoutant.blokish;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/** Confirmation dialog with icon-based affirmative and negative actions. */
public class IconDialog extends Dialog {
	private OnClick listener;

	/**
	 * Creates a dialog whose affirmative action invokes a listener supplied later.
	 *
	 * @param context context hosting the dialog
	 * @param title_id string resource used as the dialog title
	 */
	public IconDialog(final Context context, int title_id) {
		super(context);
		setContentView( R.layout.simple_dialog);
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText( title_id);
		getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		findViewById( R.id.no).setOnClickListener(new View.OnClickListener() {
			/**
			 * Cancels the confirmation and dismisses the dialog.
			 * @param v negative-action view receiving the click
			 */
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		findViewById( R.id.yes).setOnClickListener(new View.OnClickListener() {
			/**
			 * Invokes the registered action and dismisses the dialog.
			 * @param v affirmative-action view receiving the click
			 */
			@Override
			public void onClick(View v) {
				listener.onClick();
				cancel();
			}
		});
	}

	/**
	 * Registers the action invoked when the user confirms the dialog.
	 * @param listener callback to run for the affirmative action
	 */
	public void setListener( final OnClick listener) {
		this.listener = listener;
	}

	/** Callback for the affirmative action in an {@link IconDialog}. */
	public interface OnClick {
		/** Performs the action confirmed by the user. */
		void onClick();
	}
}
