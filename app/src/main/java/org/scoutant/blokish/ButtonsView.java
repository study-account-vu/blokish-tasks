
package org.scoutant.blokish;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;

import org.scoutant.blokish.model.Move;

/**
 * Hosts the cancel and confirm controls used while a piece is being positioned.
 * The enclosing {@link GameView} supplies the selected piece and applies confirmed moves.
 */
public class ButtonsView extends FrameLayout {

	protected static final String tag = "ui";
	private final Vibrator vibrator;

	private Context context;
	private ImageButton cancel;
	public ImageButton ok;

	private GameView game;

	private int width;

	/**
	 * Creates the move controls and sizes their container to the area below the board.
	 *
	 * @param context Android context used to create controls and access system services
	 */
	public ButtonsView(Context context) {
		super(context);
		this.context = context;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		setVisibility(INVISIBLE);
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point pointSize = new Point();
		display.getSize(pointSize);
		width = pointSize.x;
		int height = pointSize.y;
		int h = height - width;
		setLayoutParams( new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
		cancel = button(R.drawable.cancel, doCancel, 0);
		addView(cancel );
		ok = button(R.drawable.checkmark, doOk, 1);
		addView(ok);
		setOkState( false);
	}
	
	
	/**
	 * Creates a transparent image button and anchors it to one side of the control bar.
	 * The position value selects left (0) or right (1), matching the cancel and confirm actions.
	 *
	 * @param src drawable resource displayed by the button
	 * @param l click listener invoked when the button is pressed
	 * @param position side selector: 0 for left, 1 for right
	 * @return configured image button ready to add to this view
	 */
	private ImageButton button(int src, OnClickListener l, int position) {
		ImageButton btn = new ImageButton(context);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
		int margin = Math.min( (width - 3*128)/3, 80);
		params.leftMargin = margin;
		params.rightMargin = margin;
		if (position==0) params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		if (position==1) params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		btn.setLayoutParams(params);
		btn.setImageDrawable(context.getResources().getDrawable(src));
		btn.setScaleType(ScaleType.CENTER_INSIDE);
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.setOnClickListener(l);
		return btn;
		
	}

	/**
	 * Applies the enabled appearance shared by both move controls.
	 * @param btn control whose state is updated
	 * @param state whether the control should be enabled
	 */
	protected void setState( ImageButton btn, boolean state) {
		btn.setEnabled( state);
		btn.setAlpha( state ? 0.78f : 0.196f );
	}
	
	/** Enables or disables move confirmation to reflect placement validity. */
	public void setOkState(boolean state) {
		setState(ok, state);
	}

	/** Captures the parent game view once this control is attached to the hierarchy. */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		game = (GameView) getParent();
	}
	
	/** Handles the action when the OK button is pressed.*/
	private OnClickListener doOk = new OnClickListener() {
		/**
		 * Commits the selected piece when its current placement is valid.
		 * @param v confirmation control receiving the click
		 */
		public void onClick(View v) {
			Log.d(tag, "ok...");
			PieceUI piece = game.selected;
			if (piece==null) {
				Log.e(tag, "cannot retrieve piece!");
				return;
			}
			Move move = new Move(piece.piece, piece.i, piece.j);
			boolean possible = game.game.valid( move);
			if (possible) {
				if (vibrator!=null) vibrator.vibrate(20);
				piece.movable=false;
				piece.setLongClickable(false);
				piece.setClickable(false);
				game.lasts[piece.piece.color] = piece;
				piece.invalidate();
				ButtonsView.this.setVisibility(INVISIBLE);
				ButtonsView.this.game.game.play( move);
				((GameView)getParent()).tabs[move.piece.color].setText( ""+game.game.boards.get(move.piece.color).score);
				game.selected = null;
				game.ui.turn = (piece.piece.color+1)%4;
				if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ai", true)) {
					game.ui.think(game.ui.turn);
				} else {
						game.showPieces(game.ui.turn);
						game.invalidate();
				}
			}
		}
	};

	/** Handles cancellation by returning the selected piece to its tray and hiding the controls. */
	private OnClickListener doCancel = new OnClickListener() {
		/**
		 * Returns the selected piece to its tray without changing the model.
		 * @param v cancel control receiving the click
		 */
		public void onClick(View v) {
			if (vibrator!=null) vibrator.vibrate(20);
			Log.d(tag, "cancel...");
			game.selected.replace();
			game.selected = null;
			ButtonsView.this.setVisibility(INVISIBLE);
		}
	};
}