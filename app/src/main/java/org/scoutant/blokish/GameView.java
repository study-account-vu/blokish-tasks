
package org.scoutant.blokish;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.scoutant.blokish.model.AI;
import org.scoutant.blokish.model.Board;
import org.scoutant.blokish.model.Game;
import org.scoutant.blokish.model.Move;
import org.scoutant.blokish.model.Piece;
import org.scoutant.blokish.model.Square;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * Renders and coordinates the interactive game board and players' piece trays.
 * It connects the model in {@link Game} to {@link PieceUI} views and delegates
 * turn progression and AI work to the hosting {@link UI} activity.
 */
public class GameView extends FrameLayout {
	private static String tag = "activity";
	private final Resources rs;
	private Paint paint = new Paint();
	public int size; 
	public ButtonsView buttons;
	public PieceUI selected;
	public int selectedColor;
	public int swipe=0;
	public int gone=0;
	
	public Game game = new Game();
	public AI ai = new AI(game);
	public static int[] icons = { R.drawable.bol_rood, R.drawable.bol_groen, R.drawable.bol_blauw, R.drawable.bullet_ball_glass_yellow};
	public static int[] labels = { R.id.red, R.id.green, R.id.blue, R.id.orange};
	
	private Drawable[] dots = new Drawable[4]; 
	public TextView[] tabs = new TextView[4];
	
	public UI ui;
	public boolean redOver=false;
	public SharedPreferences prefs;
	public boolean thinking=false;
	public boolean singleline=false;
    int secondLineOffset = 0;
    int singleLineOffset = 0;
	public BusyIndicator indicator;
	public PieceUI lasts[] = new PieceUI[4];
	
	public float downX;
	public float downY;

	/**
	 * Initializes the board, piece trays, score tabs, controls, and busy indicator.
	 *
	 * @param context activity context that owns this view and provides game resources
	 */
	public GameView(Context context) {
		super(context);
		rs = context.getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ui = (UI) context;
		setWillNotDraw(false);
		setLayoutParams( new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP));
		paint.setStrokeWidth(1.3f);
		paint.setColor( getColor( R.color.white));

		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point pointSize = new Point();
		display.getSize(pointSize);
		int width = pointSize.x;
		int height = pointSize.y;

		setBackgroundColor( Color.BLACK);

		size = width/20;
		Log.d(tag, "size " + size + ", height/size : " + height/size);

		if ( height/32 < width/20) singleline = true;
        if (singleline==true && height >= size*29 ) singleLineOffset= 1;
        if (height >= size*35) secondLineOffset = 1;

		buttons = new ButtonsView(context);
		addView( buttons);

		for (Board board : game.boards) {
			int i=2;
			for (Piece piece : board.pieces) {	
				addView( new PieceUI(context, piece, i, 20+2, buttons.ok) );
				i += 4;
			}
			reorderPieces(board.color);
		}

		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.tabs, this);
		
		showPieces(0);
		for (int color=0; color<4; color++) {
			dots[color] = getDrawable(icons[color]);
			dots[color].setAlpha(191);
			tabs[color] = (TextView) findViewById( labels[color]);
			ViewGroup tab  =  (ViewGroup) tabs[color].getParent();
			if (tab!=null ) {
				tab.setOnClickListener(new ShowPiecesListener(color));
			}
		}

		View iView = new View(context);
		iView.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL));
		addView(iView);
		indicator = new BusyIndicator(context, iView);
	}

	/**
	 * Resolves a color resource using the view's current themed context.
	 * @param id color resource identifier
	 * @return resolved color value
	 */
	protected int getColor( int id) {
		return ContextCompat.getColor( getContext(), id);
	}

	/**
	 * Resolves a drawable resource using the view's current themed context.
	 * @param id drawable resource identifier
	 * @return resolved drawable, or {@code null} if unavailable
	 */
	protected Drawable getDrawable( int id) {
		return ContextCompat.getDrawable( getContext(), id);
	}

	/** Selects a player's tray when its score tab is tapped. */
		private class ShowPiecesListener implements OnClickListener {
		private int color;
		/**
		 * Creates a tab listener for one player.
		 * @param color player index whose piece tray is selected
		 */
		protected ShowPiecesListener(int color) {
			this.color = color;
		}
		/**
		 * Displays the selected player's remaining pieces.
		 * @param v tab view receiving the click
		 */
		public void onClick(View v) {
			GameView.this.showPieces(color);
			GameView.this.invalidate();
		}
	}

	/**
	 * Routes board-level swipe input unless a piece currently owns the gesture.
	 * @param event touch event delivered to this view
	 * @return {@code true} when the event is consumed
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (selected!=null) return false;
		doTouch(event);
		return true;
	}
	
	/**
	 * Updates the horizontal tray offset in response to a board swipe gesture.
	 * @param event touch event supplying the gesture coordinates and action
	 */
	public void doTouch(MotionEvent event) {
		int action = event.getAction(); 
    	if (action==MotionEvent.ACTION_DOWN) {
    		downX=event.getRawX();
    		downY=event.getRawY();
    	}
    	if (action==MotionEvent.ACTION_MOVE ) {
    		swipePieces( selectedColor, - (swipe + Float.valueOf( event.getRawX()-downX).intValue()));
    	}
    	if (action==MotionEvent.ACTION_UP ) {
    		swipe += Float.valueOf( event.getRawX()-downX).intValue();
    		downX=0;
    		swipePieces( selectedColor, -swipe);
    	}
	}
	
	/**
	 * Draws the 20-by-20 grid and, when enabled, the current player's legal seed markers.
	 * @param canvas drawing surface supplied by the view system
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < 20; i++) {
			canvas.drawLine(i*size, 0, i*size, 20*size, paint);
		}
		canvas.drawLine(20*size-1, 0, 20*size-1, 20*size, paint);
		for (int j = 0; j <= 20; j++) {
			canvas.drawLine(0, j*size+1, 20*size, j*size+1, paint);
		}
		if (prefs.getBoolean("displaySeeds", true)) {
			for (Square s : game.boards.get(selectedColor).seeds()) { 
				dots[selectedColor].setBounds( s.i*size+size/4, s.j*size+size/4, s.i*size+3*size/4, s.j*size+3*size/4);
				dots[selectedColor].draw(canvas);
			}
		}
	}
	
	/**
	 * Finds the view representing a piece by its owner color and shape identifier.
	 * @param color owning player index
	 * @param type stable shape identifier
	 * @return matching view, or {@code null} when it is not present
	 */
	public PieceUI findPiece(int color, String type) {
		PieceUI found=null;
		for (int i=0; i<getChildCount(); i++) {
			View v = getChildAt(i);
			if (v instanceof PieceUI) {
				found = (PieceUI) v;
				if (found.piece.color == color && found.piece.type == type) return found;
			}
		}
		return null;
	}

	/**
	 * Finds the view corresponding to a model piece.
	 * @param piece model piece whose view is requested
	 * @return matching view, or {@code null} when it is not present
	 */
	public PieceUI findPiece(Piece piece) {
		return findPiece(piece.color, piece.type);
	}
	
	/**
	 * Applies a model move, updates its piece view and score, and refreshes the board.
	 * @param move move to apply; {@code null} is ignored
	 * @param animate whether the placed piece should use its placement animation
	 */
	public void play(Move move, boolean animate) {
		if (move==null) return;
		PieceUI ui = findPiece( move.piece);
		boolean done = game.play(move);
		if (done) {
			lasts[ui.piece.color] = ui;
			ui.place(move.i, move.j, animate);
		}
		tabs[move.piece.color].setText( ""+game.boards.get(move.piece.color).score);
		mayReorderPieces();
		invalidate();
	}
	
	/**
	 * Shows only the stored pieces belonging to the selected player color.
	 * @param color player index whose tray should be shown
	 */
	public void showPieces(int color){
		selectedColor = color;
		for (PieceUI piece : piecesInStore()) piece.setVisibility( piece.piece.color == color ? VISIBLE : INVISIBLE);
	}

	/**
	 * Applies a horizontal pixel offset to the specified player's stored pieces.
	 * @param color player index whose tray should move
	 * @param x horizontal swipe offset in pixels
	 */
	public void swipePieces( int color, int x) {
		for (PieceUI piece : piecesInStore(color)) piece.swipe(x);
	}

	
	/** Periodically recalculates tray order as pieces are removed from play. */
	public void mayReorderPieces() {
		gone++;
		if (gone>=8) {
			gone = 0;
			reorderPieces();
		}
	}
	
	/** Repositions the remaining pieces in all four player trays. */
	public void reorderPieces() {
		for (int p=0; p<4; p++) reorderPieces( p);
	}

	/**
	 * Sorts and lays out one player's remaining pieces within one or two tray rows.
	 * @param color player index whose tray should be arranged
	 */
	public void reorderPieces( int color) {
		List<PieceUI> pieces = piecesInStore(color);
		Collections.sort(pieces);
		Collections.reverse(pieces);
		for (int p=0; p<pieces.size(); p++) {
			PieceUI piece = pieces.get(p);
			if (singleline) {
				piece.j0 = 22 + singleLineOffset;
				if (p<1) {
					if (piece.piece.type.equals("I5")) piece.i0 = 1;
					else piece.i0 = 2;
				} else {
					piece.i0 = pieces.get(p-1).i0 + pieces.get(p-1).piece.size+1;
				}
			} else {
				piece.j0 = 22 + ((p%2) > 0 ? 5+secondLineOffset : 0 ) ;

				if (p<2) {
					if (piece.piece.type.equals("I5")) piece.i0 = 1;
					else piece.i0 = 2;
				} else {
					piece.i0 = pieces.get(p-2).i0 + pieces.get(p-2).piece.size+1;
				}
			}
			piece.replace();
		}
	}

	/** Collects movable piece views, excluding pieces already placed on the board. */
	private List<PieceUI> piecesInStore(){
		List<PieceUI> list = new ArrayList<PieceUI>(); 
		for (int k=0; k<this.getChildCount(); k++) {
			if (this.getChildAt(k) instanceof PieceUI) { 
				PieceUI piece = (PieceUI) this.getChildAt(k);
				if (piece!=null && piece.movable) {
					list.add(piece);
				}
			}
		}
		return list;
	}
	
	/** Returns the stored piece views owned by one player. */
	private List<PieceUI> piecesInStore(int color){
		List<PieceUI> list = new ArrayList<PieceUI>();
		for (PieceUI piece : piecesInStore()) {
			if (piece.piece.color == color) list.add(piece);
		}
		return list;
	}

	/**
	 * Reconstructs the displayed board by applying the supplied moves without animation.
	 * @param moves ordered moves to replay
	 * @return {@code true} after the supplied sequence has been applied
	 */
	public boolean replay(List<Move> moves) {
		for (Move move : moves) {
			Piece piece = move.piece;
			PieceUI ui = findPiece(piece);
			ui.piece.reset(piece);
			play(move, false);
		}
		return true;
	}	
}