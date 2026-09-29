
package org.scoutant.blokish;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.scoutant.blokish.model.Move;
import org.scoutant.blokish.model.Piece;
import org.scoutant.blokish.model.Square;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Main game activity coordinating navigation, persistence, player turns, and AI work.
 * The activity owns {@link GameView}, restores saved moves on startup, and presents
 * dialogs when a game or a player turn concludes.
 */
public class UI extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
	private static final int MENU_ITEM_HISTORY = 99;
	private static final int MENU_ITEM_REPLAY = 101;
	private static final int MENU_ITEM_BACK = 102;
	private static final int MENU_ITEM_NEW = 5;
	private static final int MENU_ITEM_THINK=10;
	private static final int MENU_ITEM_PREFERENCES=-1;
	private static final int MENU_ITEM_HELP = 9;
	private static final int MENU_ITEM_PASS_TURN = 12;
	private static final int MENU_ITEM_FLIP = 15;

	private static String tag = "activity";
	public GameView game;
	public boolean devmode=false;
	private SharedPreferences prefs;
	private Vibrator vibrator;
	private Resources rs;
	private boolean back_pressed;
	private DrawerLayout drawer;

	public int turn = 0;
	private AITask task = null;
	
	/**
	 * Initializes the activity, creates a game surface, and restores a saved game if present.
	 * @param savedInstanceState previously saved activity state, if any
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rs = getResources();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

		newgame();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		sourceFromMovesFile();

	}

	/** Replaces the current game surface with a fresh game and reconnects navigation. */
	private void newgame() {
		game = new GameView(UI.this);
		setContentView( R.layout.activity_main);
		FrameLayout container = (FrameLayout) findViewById(R.id.container);
		container.addView( game);
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
			/**
			 * Leaves drawer slide motion to the standard drawer implementation.
			 * @param drawerView drawer currently moving
			 * @param slideOffset normalized distance from closed to open
			 */
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {}
			/**
			 * Updates flip-action visibility to match the current piece selection.
			 * @param drawerView drawer that has opened
			 */
			@Override
			public void onDrawerOpened(View drawerView) {
				navigationView.getMenu().findItem(R.id.item_flip).setVisible( game.selected!=null);
			}
			/**
			 * Performs no additional work when the drawer closes.
			 * @param drawerView drawer that has closed
			 */
			@Override
			public void onDrawerClosed(View drawerView) {}
			/**
			 * Leaves drawer state transitions to the standard drawer implementation.
			 * @param newState updated drawer state constant
			 */
			@Override
			public void onDrawerStateChanged(int newState) {}
		});

	}

	/** Handles a refresh gesture; the game has no separate pull-to-refresh action. */
	@Override
	public void onRefresh() {

	}

	/**
	 * Rebuilds the options menu to reflect selection, settings, and game mode.
	 * @param menu menu whose items are rebuilt
	 * @return {@code true} to display the prepared menu
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (game.selected!=null) {
			menu.add(Menu.NONE, MENU_ITEM_FLIP, Menu.NONE, R.string.flip).setIcon(android.R.drawable.ic_menu_set_as);
		}
		menu.add(Menu.NONE, MENU_ITEM_BACK, Menu.NONE, R.string.undo).setIcon( R.drawable.left_48);
		menu.add(Menu.NONE, MENU_ITEM_NEW, Menu.NONE, R.string.new_game).setIcon( R.drawable.restart_48);

		menu.add(Menu.NONE, MENU_ITEM_HELP, Menu.NONE, R.string.help).setIcon( R.drawable.help_48);
		menu.add(Menu.NONE, MENU_ITEM_PREFERENCES, Menu.NONE, R.string.preferences).setIcon( R.drawable.preferences_48);

		if (devmode) {
			menu.add(Menu.NONE, MENU_ITEM_THINK, Menu.NONE, "AI").setIcon(android.R.drawable.ic_menu_manage);
			menu.add(Menu.NONE, MENU_ITEM_HISTORY, Menu.NONE, "hist").setIcon(android.R.drawable.ic_menu_recent_history);
		}
		if (!prefs.getBoolean("ai", true)) {
			menu.add(Menu.NONE, MENU_ITEM_PASS_TURN, Menu.NONE, R.string.i_pass).setIcon( R.drawable.checkmark_48);			
		}
		return true;
	}
	
	/**
	 * Dispatches an options-menu action and updates game or activity state.
	 * @param item selected menu item
	 * @return {@code false} after processing the action
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == MENU_ITEM_HELP) {
			startActivity(new Intent(this, Help.class));
		}
		if (item.getItemId() == MENU_ITEM_PREFERENCES) {
			startActivity(new Intent(this, Settings.class));
		}
		if (item.getItemId() == MENU_ITEM_HISTORY) {
			Log.d(tag, "" + game.game);
		}
		if (item.getItemId() == MENU_ITEM_REPLAY) {
			GameView old = game;
			newgame();
			Log.d(tag, "replay # moves : " + old.game.moves.size());
			game.replay(old.game.moves);
		}
		if (item.getItemId() == MENU_ITEM_BACK) {
			List<Move> moves = game.game.moves;
			int length = moves.size();
			if (length>=4) {
				length -= 4;
			}
			moves = moves.subList(0, length);
			newgame();
			Log.i(tag, "replay # moves : " + length);
			game.replay( moves);			
		}
		if (item.getItemId() == MENU_ITEM_NEW) {
			final AlertDialog dialog =
			new AlertDialog.Builder(this)
			.setMessage(rs.getString(R.string.new_game) + "?")
			.setCancelable(false)
			.setPositiveButton(" ", new DialogInterface.OnClickListener() {
				/**
				 * Starts a fresh game after confirmation.
				 * @param dialog new-game confirmation dialog
				 * @param which selected dialog button identifier
				 */
				public void onClick(DialogInterface dialog, int which) {
					newgame();
				}
			})
			.setNegativeButton(" ", new DialogInterface.OnClickListener() {
				/**
				 * Dismisses the confirmation without replacing the current game.
				 * @param dialog new-game confirmation dialog
				 * @param id selected dialog button identifier
				 */
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			})
			.create();
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				/**
				 * Applies the custom action icons once the dialog buttons exist.
				 * @param dialogInterface dialog whose buttons are now available
				 */
				@Override
				public void onShow(DialogInterface dialogInterface) {
					setButtonImage(dialog, AlertDialog.BUTTON_POSITIVE, R.drawable.checkmark);
					setButtonImage(dialog, AlertDialog.BUTTON_NEGATIVE, R.drawable.cancel);
				}
			});
			dialog.show();
			}
		if (item.getItemId() == MENU_ITEM_THINK) {
			think(0);
		}
		if (item.getItemId() == MENU_ITEM_PASS_TURN) {
				turn = (turn + 1) % 4;
				game.showPieces(turn);
				game.invalidate();
		}
		if (item.getItemId() == MENU_ITEM_FLIP) {
			PieceUI piece = game.selected;
			if (piece!=null) piece.flip();
		}
		return false;
	}

	/**
	 * Dispatches a navigation drawer action, closing the drawer after selection.
	 * @param item selected navigation item
	 * @return {@code true} after handling the selection
	 */
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		Log.d("menu", "menu item is : " + item);
		int id = item.getItemId();
		if (id==R.id.item_help) startActivity(new Intent(this, Help.class));
		if (id==R.id.item_preferences) startActivity(new Intent(this, Settings.class));
		if (id==R.id.item_back) {
			new Handler().postDelayed(new Runnable() {
				/** Replays the saved history with the most recent turn removed. */
				@Override
				public void run() {
					List<Move> moves = game.game.moves;
					int length = moves.size();
					if (length>=4) {
						length -= 4;
					}
					moves = moves.subList(0, length);
					newgame();
					Log.i(tag, "replay # moves : " + length);
					game.replay( moves);
				}
			}, 500);
		}
		if (id==R.id.item_new) {

			final IconDialog dialog = new IconDialog(this, R.string.new_game);
			dialog.setListener(new IconDialog.OnClick() {
				/** Replaces the active match with a new game. */
				@Override
				public void onClick() {
					newgame();
				}
			});
			dialog.show();

		}
		if (id==R.id.item_flip) {

			new Handler().postDelayed(new Runnable() {
				/** Defers selected-piece flipping until the drawer has closed. */
				@Override
				public void run() {
					final PieceUI piece = game.selected;
					if (piece!=null) {
						runOnUiThread(new Runnable() {
							/** Flips the piece and refreshes its candidate-validity indication. */
							@Override
							public void run() {
								piece.flip();
								boolean okState = game.game.valid(piece.piece, piece.i, piece.j);
								piece.setOkState( okState);
								game.buttons.setOkState( okState);
								game.invalidate();
							}
						});
					}
				}
			}, 500);
		}

		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	/**
	 * Replaces a dialog button's text presentation with the supplied drawable.
	 * @param dialog dialog containing the button
	 * @param buttonId alert-dialog button identifier
	 * @param id drawable resource identifier
	 */
	private void setButtonImage( AlertDialog dialog, int buttonId, int id ) {
		Button button = dialog.getButton( buttonId);
		Drawable drawable = getResources().getDrawable( id);
		drawable.setBounds(drawable.getIntrinsicWidth()/4, 0, drawable.getIntrinsicWidth()*3/4, drawable.getIntrinsicHeight()/2);
		button.setCompoundDrawables(drawable, null, null, null);
	}

	/**
	 * Starts asynchronous AI processing for the given player and makes it the active turn.
	 * @param player player index whose AI move is requested
	 */
	public void think(int player) {
		turn = player;
		new AITask().execute(player);
	}
	
	/** Returns the difficulty selected in preferences, before runtime adaptation. */
	private int findRequestedLevel() {
		String level = prefs.getString("aiLevel", "0");
		return Integer.valueOf(level);
	}
	
	/** Bounds the requested difficulty by the level currently supported at runtime. */
	private int findLevel() {
		String level = prefs.getString("aiLevel", "0");
		int l = Integer.valueOf(level);
		if (l<0 || l>3) l = 1;
		return Math.min(l, game.ai.adaptedLevel);
	}
		

	/** Computes and applies one AI move, then advances or concludes the turn sequence. */
	private class AITask extends AsyncTask<Integer, Void, Move> {
		/**
		 * Calculates the selected player's move away from the UI thread.
		 * @param params one-element array containing the player index
		 * @return selected move, or {@code null} when none is available
		 */
		@Override
		protected Move doInBackground(Integer... params) {
			task = this;
			game.thinking=true;
			game.indicator.show();
			return game.ai.think(params[0], findLevel());
		}
		/**
		 * Applies the calculated move and schedules the next player or completion check.
		 * @param move move returned by the background search
		 */
		@Override
		protected void onPostExecute(Move move) {
			if (vibrator!=null && !game.redOver) vibrator.vibrate(15);
			if (game.game.over()) {
				displayWinnerDialog();
				return;
			}
			UI.this.game.play( move, true);
			turn++;
			if (turn<4) new AITask().execute(turn);
			if (turn==4) game.indicator.hide();
			if (turn==4 && !game.redOver ) {
				game.thinking=false;
				turn=0;
				game.showPieces(0);
				new CheckTask().execute();
			}
			if (turn==4 && game.redOver ) {
				Log.d(tag, "Red is dead. game.over ? " + game.game.over());
				if (game.game.over()) {
					displayWinnerDialog();					
				} else {
					turn = 1;
					new AITask().execute(turn);
				}
			}
		}
		/** Builds the final score message and displays the game result. */
		private void displayWinnerDialog() {
			game.indicator.hide();
			Log.d(tag, "game over !");
			int winner = game.game.winner();
			int score = game.game.boards.get(winner).score;
			String message = "";
			boolean redWins = (winner==0 && prefs.getBoolean("ai", true));
			if (redWins) {
				 message += rs.getString( R.string.congratulations) + " " + score +".";
				 if (findRequestedLevel()<(4-1)) message += "\n" + rs.getString( R.string.try_next);
			} else {
				message += rs.getString( game.game.colors[winner]);
				message += " " + rs.getString( R.string.wins_with_score) + " : ";
				message += score;
			}
			new EndGameDialog(UI.this, redWins, message, findRequestedLevel()+1, score).show();
		}
	}
	
	/** Checks whether the human-controlled player has any legal move remaining. */
	private class CheckTask extends AsyncTask<Void, Void, Boolean> {
		/**
		 * Queries the game model for a legal move off the UI thread.
		 * @param params unused task parameters
		 * @return {@code true} if the red player has no legal move
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
			return !game.ai.hasMove(0);
		}
		/**
		 * Prompts the user to acknowledge a blocked red player when no move exists.
		 * @param finished whether the background check found no legal move
		 */
		@Override
		protected void onPostExecute(Boolean finished) {
			if (finished) {
				game.indicator.hide();
				Log.d(tag, "red over!");
				final AlertDialog dialog =
				new AlertDialog.Builder(UI.this)
				.setMessage( R.string.red_ko)
				.setCancelable(false)
				.setPositiveButton(" ", new DialogInterface.OnClickListener() {
					/**
					 * Marks the human player as passed and starts the next AI turn.
					 * @param dialog acknowledgement dialog
					 * @param which selected dialog button identifier
					 */
					public void onClick(DialogInterface dialog, int which) {
						game.redOver = true;
						game.game.boards.get(0).over = true;
						Log.d(tag, "ok!");
						think(1);
						}
					})
				.create();
				dialog.setOnShowListener(new DialogInterface.OnShowListener() {
					/**
					 * Applies the confirmation icon to the acknowledgement button.
					 * @param dialogInterface acknowledgement dialog
					 */
					@Override
					public void onShow(DialogInterface dialogInterface) {
						setButtonImage( dialog, AlertDialog.BUTTON_POSITIVE, R.drawable.checkmark);
					}
				});
				dialog.show();
			}
		}
	}

	private Toast toast;
	/** Closes an open drawer or requires a second press before leaving the activity. */
	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
			return;
		}

		if (back_pressed==true) {
			if (toast!=null) toast.cancel();
			super.onBackPressed();
			return;
		}
		toast = Toast.makeText( this, R.string.twice_to_exit, Toast.LENGTH_SHORT);
		toast.show();
		back_pressed = true;
	}

	/** Writes the current move history to the app-private save file. */
	private void saveToMovesFile() {
		try {
			FileOutputStream fos = openFileOutput("moves.txt", Context.MODE_PRIVATE);
			save(fos);
		} catch (FileNotFoundException e) {
			Log.e(tag, "not found...", e);
		}
	}

	/** Serializes an unfinished game's history to the supplied stream and closes it. */
	private void save(OutputStream os){
		try {
			if (os==null) return;
			if (!game.game.over()) {
				os.write( game.game.toString().getBytes());
			}  
			os.close();
		} catch (FileNotFoundException e) {
			Log.e(tag, "not found...", e);
		} catch (IOException e) {
			Log.e(tag, "io...", e);
		}
	}


	/** Restores game history from the app-private save file when it is available. */
	private void sourceFromMovesFile() {
		try {
			FileInputStream fis = openFileInput("moves.txt");
			source(fis);
		} catch (IOException e) {
			Log.e(tag, "yep error is :", e);
		}
	}



	/** Parses serialized move records, rebuilds their pieces, and replays the resulting history. */
	private void source(InputStream is) {
		List<Move> list = new ArrayList<Move>();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader(is));
			String line;
			reader.readLine();  
			while ((line = reader.readLine()) != null)   {
				String[] data = line.split(":");
				int i = Integer.valueOf(data[0]);
				int j = Integer.valueOf(data[1]);
				int color = Integer.valueOf(data[2]);
				Piece piece = game.game.boards.get(color).findPieceByType(data[3] );
				piece.reset();
				for (int q = 4; q<data.length; q++) {
					String[] position = data[q].split(",");
					int x = Integer.valueOf( position[0]);
					int y = Integer.valueOf( position[1]);
					piece.add( new Square(x, y ));
				}
				Move move = new Move(piece, i, j);
				list.add(move);
			}
			newgame();
			game.replay(list);
			game.reorderPieces();
		} catch (Exception e) {
			Log.e(tag, "yep error is :", e);
		}
	}

	/** Cancels outstanding AI work and persists the current game before the activity stops. */
	@Override
	protected void onStop() {
		if (task!=null) {
			task.cancel(true);
			Log.d(tag, "leaving AI, as activity is brough to background");
		}
			saveToMovesFile();
		super.onStop();
	}
	
}