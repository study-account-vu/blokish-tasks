/*
* Copyright (C) 2011- stephane coutant
*
* This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>
*/

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
 * For DnD approach, refer to http://blog.scoutant.org/index.php?post/2011/02/Approche-naturelle-de-Drag-and-Drop-en-Android
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
	/** true if red has acknowledged no more moves for her */
	public boolean redOver=false;
	public SharedPreferences prefs;
	public boolean thinking=false;
	public boolean singleline=false;
    int secondLineOffset = 0;
    int singleLineOffset = 0;
	public BusyIndicator indicator;
	public PieceUI lasts[] = new PieceUI[4];
	
	// Creates the board, piece tray, controls, and player tabs.
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
//				addView( new PieceUI(context, piece, i, 20+2) );
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
			// let put the listener on the parent view group
			ViewGroup tab  =  (ViewGroup) tabs[color].getParent();
			if (tab!=null ) {
				tab.setOnClickListener(new ShowPiecesListener(color));
			}
		}

		// progress indicator
		View iView = new View(context);
		iView.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL));
		addView(iView);
		indicator = new BusyIndicator(context, iView);
	}

	// Resolves a color resource for this view.
	protected int getColor( int id) {
		return ContextCompat.getColor( getContext(), id);
	}
	// Resolves a drawable resource for this view.
	protected Drawable getDrawable( int id) {
		return ContextCompat.getDrawable( getContext(), id);
	}


		private class ShowPiecesListener implements OnClickListener {
		private int color;
		// Creates a tab listener for the given player color.
		protected ShowPiecesListener(int color) {
			this.color = color;
		}
		// Displays the selected player's available pieces.
		public void onClick(View v) {
			GameView.this.showPieces(color);
			GameView.this.invalidate();
		}
	}
	
	public float downX;
	public float downY;

	// Handles touch gestures received by the board.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (selected!=null) return false;
		doTouch(event);
		return true;
	}
	
	// Tracks horizontal gestures for scrolling the piece tray.
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
	
	// Draws the board grid and available seed markers.
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
	
	// Finds a piece view by player color and piece type.
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

	// Finds the view that displays the given piece.
	public PieceUI findPiece(Piece piece) {
		return findPiece(piece.color, piece.type);
	}
	
	// Plays a move and updates its visual representation.
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
	
	// Shows the available pieces for the given player.
	public void showPieces(int color){
		selectedColor = color;
		for (PieceUI piece : piecesInStore()) piece.setVisibility( piece.piece.color == color ? VISIBLE : INVISIBLE);
	}

	// Scrolls the given player's piece tray.
	public void swipePieces( int color, int x) {
		for (PieceUI piece : piecesInStore(color)) piece.swipe(x);
	}

	
	// Reorders pieces after enough moves have been played.
	public void mayReorderPieces() {
		gone++;
		if (gone>=8) {
			gone = 0;
			reorderPieces();
		}
	}
	
	// Reorders the available pieces for every player.
	public void reorderPieces() {
		for (int p=0; p<4; p++) reorderPieces( p);
	}

	// Repositions the available pieces for one player.
	public void reorderPieces( int color) {
		List<PieceUI> pieces = piecesInStore(color);
		Collections.sort(pieces);
		Collections.reverse(pieces);
		for (int p=0; p<pieces.size(); p++) {
			PieceUI piece = pieces.get(p);
			if (singleline) {
//				piece.j0 = 22;
				piece.j0 = 22 + singleLineOffset;
				if (p<1) {
					if (piece.piece.type.equals("I5")) piece.i0 = 1;
					else piece.i0 = 2;
				} else {
					piece.i0 = pieces.get(p-1).i0 + pieces.get(p-1).piece.size+1;
				}
			} else {
//				piece.j0 = 22 + ((p%2) > 0 ? 5 : 0 ) ;
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

	// Returns every movable piece currently in the tray.
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
	// Returns movable tray pieces for one player.
	private List<PieceUI> piecesInStore(int color){
		List<PieceUI> list = new ArrayList<PieceUI>();
		for (PieceUI piece : piecesInStore()) {
			if (piece.piece.color == color) list.add(piece);
		}
		return list;
	}

	// Replays moves in the view without animation.
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