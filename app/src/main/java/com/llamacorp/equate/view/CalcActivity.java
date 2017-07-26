package com.llamacorp.equate.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.R;
import com.llamacorp.equate.view.ConvKeysFragment.OnConvertKeySelectedListener;
import com.llamacorp.equate.view.IdlingResource.SimpleIdlingResource;
import com.viewpagerindicator.TabPageIndicator;

import java.util.HashSet;
import java.util.Set;

public class CalcActivity extends AppCompatActivity
		  implements ResultListFragment.UnitSelectListener, OnConvertKeySelectedListener,
		  NavigationView.OnNavigationItemSelectedListener {
	private static final int[] BUTTON_IDS = {
			  R.id.zero_button, R.id.one_button, R.id.two_button, R.id.three_button,
			  R.id.four_button, R.id.five_button, R.id.six_button, R.id.seven_button,
			  R.id.eight_button, R.id.nine_button,

			  R.id.plus_button,
			  R.id.minus_button,
			  R.id.multiply_button,
			  R.id.divide_button,
			  R.id.percent_button,

			  R.id.decimal_button,
			  R.id.equals_button,
			  //		R.id.ee_button,
			  //		R.id.power_button,

			  R.id.clear_button,

			  R.id.open_para_button,
			  R.id.close_para_button,

	};
	private Context mAppContext;  //used for toasts and the like

	private ResultListFragment mResultListFrag;   //scroll-able history
	private EditTextDisplay mDisplay;      //main display
	private ViewPager mUnitTypeViewPager;         //controls and displays UnitType
	private DynamicTextView mResultPreview;   //Result preview
	private UnitSearchDialogBuilder mSearchDialogBuilder; // Unit search dialog

	// (Used for test) Idling Resource which will be null in production.
	@Nullable
	private SimpleIdlingResource mIdlingResource;

	private Button mEqualsButton; //used for changing color
	//main calculator object
	private Calculator mCalc;
	//Crude fix: used to tell the ConvKeyViewPager what unit to select after
	// scrolling to correct UnitType
	private int unitPosToSelectAfterScroll = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppContext = this;
		setContentView(R.layout.drawer_layout);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		//either get old calc or create a new one
		mCalc = Calculator.getCalculator(this);

		//main result display
		mDisplay = (EditTextDisplay) findViewById(R.id.textDisplay);
		mResultPreview = (DynamicTextView) findViewById(R.id.resultPreview);
		mDisplay.setCalc(mCalc);
		mDisplay.disableSoftInputFromAppearing();


		//we don't want the text view to go to two lines ever. this fixes that
		mResultPreview.setHorizontallyScrolling(true);

		mResultPreview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				numButtonPressed("=");
			}
		});

		mResultPreview.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				CharSequence copiedText = mResultPreview.getText();

				ClipboardManager clipboard = (ClipboardManager) mAppContext.getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setPrimaryClip(ClipData.newPlainText(null, copiedText));

				ViewUtils.toast("Copied: \"" + copiedText + "\"", mAppContext);
				return true;
			}
		});

		//keyboard hiding wasn't working on Samsung device, brute force instead
		mDisplay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//ViewUtils.toast("onClick",mAppContext);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mDisplay.getWindowToken(), 0);
			}
		});

		//hold click will select all text
		mDisplay.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
//				//keyboard hiding wasn't working on Samsung device, brute force instead
//				ViewUtils.toast("on long click",mAppContext);
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(mDisplay.getWindowToken(), 0);
				mDisplay.selectAll();
				return false;
			}
		});

		//clicking display will set solve=false, and will make the cursor visible
		mDisplay.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					//once the user clicks on part of the expression, don't want # to delete it
					mCalc.setSolved(false);
					mDisplay.setCursorVisible(true);
					mDisplay.clearHighlighted();
				}
//				else if(event.getAction()==MotionEvent.ACTION_UP){
//					ViewUtils.toast("Action Up",mAppContext);
//					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.hideSoftInputFromWindow(mDisplay.getWindowToken(), 0);
//					return true;
//				}
				return false;
			}

		});

//		mDisplay.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//			@Override
//			public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//				Log.d("DBG", "in on create context menu");
//			}
//		});

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();
		mResultListFrag = (ResultListFragment) fm.findFragmentById(R.id.resultListFragmentContainer);

		if (mResultListFrag == null){
			mResultListFrag = new ResultListFragment();
			fm.beginTransaction().add(R.id.resultListFragmentContainer, mResultListFrag).commit();
		}


		for (int id : BUTTON_IDS) {
			final Button button = (Button) findViewById(id);

			//used for coloring the equals button
			if (id == R.id.equals_button) mEqualsButton = button;

			if (id == R.id.percent_button){
				((AnimatedHoldButton) button)
						  .setPrimaryText(mCalc.mPreferences.getPercentButMain());
				((AnimatedHoldButton) button)
						  .setSecondaryText(mCalc.mPreferences.getPercentButSec());
			}

//
//			button.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View view, MotionEvent motionEvent) {
//					if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
//						mTimer = System.currentTimeMillis();
//						Log.d("buttonTimer", "Time pressed = " +
//								  String.valueOf(System.currentTimeMillis() - mTimer));
//					}
//					if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP){
//						Log.d("buttonTimer", "Time pressed = " +
//								  String.valueOf(System.currentTimeMillis() - mTimer));
//					}
//					return false;
//				}
//			});

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int buttonId = view.getId();
					String buttonValue = "";
					switch (buttonId) {
						case R.id.plus_button:
							buttonValue = "+";
							break;
						case R.id.minus_button:
							buttonValue = "-";
							break;
						case R.id.multiply_button:
							buttonValue = "*";
							break;
						case R.id.divide_button:
							buttonValue = "/";
							break;
						case R.id.percent_button:
							if (mCalc.mPreferences.getPercentButMain().equals("%"))
								buttonValue = "%";
							else
								buttonValue = "E";
							break;
						case R.id.decimal_button:
							buttonValue = ".";
							break;
						case R.id.equals_button:
							buttonValue = "=";
							break;
						//					case R.id.ee_button: buttonValue="E";
						//					break;
						//					case R.id.power_button: buttonValue="^";
						//					break;
						case R.id.clear_button:
							buttonValue = "c";
							break;
						case R.id.open_para_button:
							buttonValue = "(";
							break;
						case R.id.close_para_button:
							buttonValue = ")";
							break;
						case R.id.backspace_button:
							buttonValue = "b";
							break;
						default:
							//this for loop checks for numerical values
							for (int i = 0; i < 10; i++)
								if (buttonId == BUTTON_IDS[i])
									buttonValue = String.valueOf(i);
					}
					//pass button to calc, change conv key colors (maybe) and update screen
					numButtonPressed(buttonValue);
				}
			});

			button.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					int buttonId = view.getId();
					String buttonValue = "";
					switch (buttonId) {
						case R.id.multiply_button:
							buttonValue = "^";
							break;
						case R.id.equals_button:
							//buttonValue = "g";
							DrawerLayout drawer =
									  (DrawerLayout) findViewById(R.id.drawer_layout);
							drawer.openDrawer(GravityCompat.START);
							break;
						case R.id.percent_button:
							if (mCalc.mPreferences.getPercentButSec().equals("EE"))
								buttonValue = "E";
							else
								buttonValue = "%";
							break;
						case R.id.nine_button:
							mCalc.refreshAllDynamicUnits(true);
							break;
						case R.id.minus_button:
							buttonValue = "n";
							break;
						case R.id.divide_button:
							buttonValue = "i";
							break;
						case R.id.eight_button:
							setUnitViewVisibility(UnitVisibility.TOGGLE);
							break;
						case R.id.open_para_button:
							buttonValue = "[";
							break;
						case R.id.close_para_button:
							buttonValue = "]";
							break;
						default:
							return false;
					}
					//pass button to calc, change conv key colors (maybe) and update screen
					if (!buttonValue.equals(""))
						numButtonPressed(buttonValue);
					return true;
				}
			});

			//extra long click for buttons with settings
			if (button instanceof AnimatedHoldButton){
				final AnimatedHoldButton ahb = (AnimatedHoldButton) button;
				ahb.setOnExtraLongClickListener(new AnimatedHoldButton.OnExtraLongClickListener() {
					@Override
					public void onExtraLongClick(View view) {
						int buttonId = view.getId();
						if (buttonId == R.id.percent_button){
							//TODO add code to pop up dialog to switch buttons
							//TODO dialog reads "Set primary button function:"
							//TODO options will be %, E, ^, 1/x, and +/-
							//simple swap
							String main = mCalc.mPreferences.getPercentButMain();
							String sec = mCalc.mPreferences.getPercentButSec();
							mCalc.mPreferences.setPercentButMain(sec);
							mCalc.mPreferences.setPercentButSec(main);
							ViewUtils.toastLong("Button changed to " + sec, mAppContext);
							ahb.setPrimaryText(sec);
							ahb.setSecondaryText(main);
							ahb.invalidate();
						}
					}
				});
			}
		}


		ImageButton backspaceButton = (ImageButton) findViewById(R.id.backspace_button);
		backspaceButton.setOnTouchListener(new View.OnTouchListener() {
			private static final int RESET_HOLD_TIME = 2200;
			private final int CLEAR_HOLD_TIME = ViewUtils.getLongClickTimeout(mAppContext);
			Runnable mBackspaceReset = new Runnable() {
				@Override
				public void run() {
					resetCalculator();
				}
			};
			private Handler mColorHoldHandler;
			private Handler mResetHandler;
			//private int startTime;
			private View mView;
			private int mInc;
			//set up the runnable for when backspace is held down
			Runnable mBackspaceColor = new Runnable() {
				private static final int NUM_COLOR_CHANGES = 10;
				private int mStartColor = ContextCompat.getColor(mAppContext, R.color.op_button_pressed);
				private int mEndColor = ContextCompat.getColor(mAppContext, R.color.backspace_button_held);

				@Override
				public void run() {
					//after clear had been performed and 100ms is up, set color back to default
					if (mInc == -1){
						mView.setBackgroundColor(mEndColor);
						return;
					}
					//color the button black for a second and then clear
					if (mInc == NUM_COLOR_CHANGES){
						numButtonPressed("c");
						mView.setBackgroundColor(Color.argb(255, 0, 0, 0));
						mColorHoldHandler.postDelayed(this, 100);
						mInc = -1;
						return;
					}
					mColorHoldHandler.postDelayed(this, CLEAR_HOLD_TIME / NUM_COLOR_CHANGES);

					float deltaRed = (float) Color.red(mStartColor) + ((float) Color.red(mEndColor) - (float) Color.red(mStartColor)) * ((float) mInc * (float) mInc * (float) mInc) / ((float) NUM_COLOR_CHANGES * (float) NUM_COLOR_CHANGES * (float) NUM_COLOR_CHANGES);

					int deltaGreen = Color.green(mStartColor) + ((Color.green(mEndColor) - Color.green(mStartColor)) * mInc) / NUM_COLOR_CHANGES;
					int deltaBlue = Color.blue(mStartColor) + ((Color.blue(mEndColor) - Color.blue(mStartColor)) * mInc) / NUM_COLOR_CHANGES;

					mView.setBackgroundColor(Color.argb(255, (int) deltaRed, deltaGreen, deltaBlue));
					mInc++;
				}
			};

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mView = view;
						mInc = 0;

						if (mColorHoldHandler != null) return true;
						mColorHoldHandler = new Handler();
						mColorHoldHandler.postDelayed(mBackspaceColor, 10);

						if (mResetHandler != null) return true;
						mResetHandler = new Handler();
						mResetHandler.postDelayed(mBackspaceReset, RESET_HOLD_TIME);

						break;
					case MotionEvent.ACTION_UP:
						if (mColorHoldHandler == null) return true;
						if (mResetHandler == null) return true;
						numButtonPressed("b");
						view.setBackgroundColor(ContextCompat.getColor(mAppContext, R.color.op_button_normal));

						mColorHoldHandler.removeCallbacks(mBackspaceColor);
						mColorHoldHandler = null;

						mResetHandler.removeCallbacks(mBackspaceReset);
						mResetHandler = null;
						break;
				}
				return false;
			}
		});
	}

	private void setupUnitTypePager() {
		//if we have no Unit Types selected from settings, don't show Units view
		if (mCalc.getUnitTypeSize() == 0){
			setUnitViewVisibility(UnitVisibility.HIDDEN);
			return;
		} else {
			setUnitViewVisibility(UnitVisibility.VISIBLE);
		}

		//use fragment manager to make the result list
		FragmentManager fm = getSupportFragmentManager();

		mUnitTypeViewPager = (ViewPager) findViewById(R.id.unit_pager);
		mUnitTypeViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public int getCount() {
				return mCalc.getUnitTypeSize();
			}

			@Override
			public Fragment getItem(int pos) {
				return ConvKeysFragment.newInstance(pos);
			}

			@Override
			public CharSequence getPageTitle(int pos) {
				return mCalc.getUnitTypeName(pos % mCalc.getUnitTypeSize());
			}
		});

		TabPageIndicator mUnitTypeTabIndicator = (TabPageIndicator) findViewById(R.id.unit_type_titles);
		mUnitTypeTabIndicator.setViewPager(mUnitTypeViewPager);
		mUnitTypeTabIndicator.setVisibility(View.VISIBLE);

		//need to tell calc when a new UnitType page is selected
		mUnitTypeTabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//as the page is being scrolled to
			@Override
			public void onPageSelected(int pos) {
				// clear unit selection from current Unit Type before switching
				mCalc.getCurrUnitType().clearUnitSelection();

				//update the calc with current UnitType selection
				mCalc.setCurrentUnitTypePos(pos);

				//if we just switched to a dynamic unit, attempt an update
				if (mCalc.getCurrUnitType().containsDynamicUnits())
					mCalc.refreshAllDynamicUnits(false);

				//clear selected unit from adjacent convert key fragment so you
				//a bit of it
				int currUnitTypePos = mUnitTypeViewPager.getCurrentItem();
				clearUnitSelection(currUnitTypePos - 1);
				clearUnitSelection(currUnitTypePos);
				clearUnitSelection(currUnitTypePos + 1);
				mCalc.getCurrUnitType().clearUnitSelection();

				//if this change in UnitType was result of unit-ed result selection,
				// select that unit
				if (unitPosToSelectAfterScroll != -1){
					ConvKeysFragment frag = getConvKeyFrag(mUnitTypeViewPager.getCurrentItem());
					if (frag != null)
						frag.selectUnitAtUnitArrayPos(unitPosToSelectAfterScroll);
					unitPosToSelectAfterScroll = -1;
				}

				//clear out the unit in expression if it's now cleared
				updateScreen(true);

				//move the cursor to the right end (helps usability a bit)
				mDisplay.setSelectionToEnd();
			}

			@Override
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		//set page back to the previously selected page
		mUnitTypeViewPager.setCurrentItem(mCalc.getUnitTypePos());
		mUnitTypeTabIndicator.notifyDataSetChanged();
	}

	/**
	 * Called when any non convert key is pressed
	 *
	 * @param keyPressed ASCII representation of the key pressed ("1", "=" "*", etc)
	 */
	public void numButtonPressed(String keyPressed) {
		//pass button value to CalcActivity to pass to calc
		Calculator.CalculatorResultFlags flags = mCalc.parseKeyPressed(keyPressed);

		if (flags.createDiffUnitDialog){
			new AlertDialog.Builder(this)
					  .setMessage("Click a different unit to convert")
					  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int which) {
						  }
					  })
					  .show();
		}

		//update the result list and do it with the normal scroll (not fast)
		updateScreen(flags.performedSolve);
	}

	public void resetCalculator() {
		mCalc.resetCalc();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.apply();

		setupUnitTypePager();

		updateScreen(true);

		ViewUtils.toastCentered("Calculator reset", mAppContext);
	}

	/**
	 * Selects the a unit (used by result list)
	 *
	 * @see ResultListFragment.UnitSelectListener
	 */
	public void selectUnitAtUnitArrayPos(int unitPos, String unitTypeKey) {
		int visibleUnitTypeIndex = mCalc.getUnitTypeIndex(unitTypeKey);

		// if Unit Type is not displayed, update prefs to set it as displayed
		if (visibleUnitTypeIndex == -1){
			//load in Unit Type arrangement prefs
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			Set<String> storedSet = sharedPref.getStringSet(
					  SettingsActivity.UNIT_TYPE_PREF_KEY, null);

			assert storedSet != null; // not sure why we'd have a null pref
			HashSet<String> selections = new HashSet<>(storedSet);
			selections.add(unitTypeKey);
			sharedPref.edit().putStringSet(
					  SettingsActivity.UNIT_TYPE_PREF_KEY, selections).apply();

			// update the selections in the calculator
			mCalc.setSelectedUnitTypes(selections);
			visibleUnitTypeIndex = mCalc.getUnitTypeIndex(unitTypeKey);

			// update our unit pager to reflect updated prefs
			setupUnitTypePager();
		}
		//if not on right page, scroll there first
		if (visibleUnitTypeIndex != mUnitTypeViewPager.getCurrentItem()){
			unitPosToSelectAfterScroll = unitPos;
			mUnitTypeViewPager.setCurrentItem(visibleUnitTypeIndex);
		} else {
			ConvKeysFragment frag = getConvKeyFrag(mUnitTypeViewPager.getCurrentItem());
			if (frag != null) frag.selectUnitAtUnitArrayPos(unitPos);
		}
	}

	private void setUnitViewVisibility(UnitVisibility uv) {
		final LinearLayout mUnitContain = (LinearLayout) findViewById(R.id.unit_container);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			if (uv == UnitVisibility.HIDDEN || mCalc.getUnitTypeSize() == 0 ||
					  (uv == UnitVisibility.TOGGLE && mUnitContain.getVisibility() == LinearLayout.VISIBLE))
				mUnitContain.setVisibility(LinearLayout.GONE);
			else {
				mUnitContain.setVisibility(LinearLayout.VISIBLE);
				//update the screen to move result list up
				updateScreen(true, true);
			}
		}
	}

	/**
	 * Grabs newest data from Calculator, updates the main display, and gives an
	 * option to scroll down the result list
	 *
	 * @param updateResult  pass true to update result list
	 * @param instantScroll pass true to scroll instantly, otherwise use animation
	 */
	private void updateScreen(boolean updateResult, boolean instantScroll) {
		mDisplay.updateTextFromCalc(); //Update EditText view

		//will preview become visible during this screen update?
		boolean makePreviewVisible = !mCalc.isSolved()
				  && !mCalc.isPreviewEmpty() && !mCalc.isUnitSelected();

		//if preview just appeared, move the history list up so the last item
		//doesn't get hidden by the preview
		if (mResultPreview.getVisibility() != View.VISIBLE && makePreviewVisible){
			updateResult = true;
			instantScroll = true;
		}

		mResultPreview.setVisibility(makePreviewVisible ? View.VISIBLE : View.GONE);

		updatePreviewText(ContextCompat.getColor(mAppContext, R.color.preview_si_suffix_text_color));

		//if we hit equals, update result list
		if (updateResult)
			mResultListFrag.refresh(instantScroll);
	}

	/**
	 * Grabs newest data from Calculator, updates the main display
	 *
	 * @param updateResult whether or not to update result
	 */
	public void updateScreen(boolean updateResult) {
		//no instant scroll for previous expression
		updateScreen(updateResult, false);

		//see if colored convert button should be not colored (if backspace or
		//clear were pressed, or if expression solved)
		if (!mCalc.isUnitSelected() && mUnitTypeViewPager != null)
			clearUnitSelection(mUnitTypeViewPager.getCurrentItem());
	}

	private void updatePreviewText(int suffixColor) {
		mResultPreview.setText(mCalc.getPreviewText(suffixColor));
	}

	/**
	 * Changes equals button color according the the input boolean value.
	 * Equals button is colored normally when button is not selected. When
	 * a unit is selected, equals button looks like a regular op button
	 */
	public void setEqualButtonColor(boolean unHighlighted) {
		mEqualsButton.setSelected(unHighlighted);
	}

	/**
	 * Clear the unit selection for unit type fragment at position pos
	 *
	 * @param unitTypeFragPos the position of the desired unit type fragment
	 *                        from which to clear selected units
	 */
	private void clearUnitSelection(int unitTypeFragPos) {
		ConvKeysFragment currFragAtPos = getConvKeyFrag(unitTypeFragPos);
		if (currFragAtPos != null)
			currFragAtPos.clearButtonSelection();
	}

	/**
	 * Helper function to return the convert key fragment at position pos
	 *
	 * @param pos the position of the desired convert key fragment
	 * @return will return the fragment or null if it doesn't exist at that position
	 */
	private ConvKeysFragment getConvKeyFrag(int pos) {
		FragmentStatePagerAdapter tempAdapter =
				  (FragmentStatePagerAdapter) mUnitTypeViewPager.getAdapter();
		//make sure we aren't trying to access an invalid page fragment
		if (pos < tempAdapter.getCount() && pos >= 0){
			return (ConvKeysFragment) tempAdapter.
					  instantiateItem(mUnitTypeViewPager, pos);
		} else return null;
	}

	/**
	 * Called when an item in the navigation menu drawer is selected
	 *
	 * @param item that is selected
	 */
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_find){
			if (mSearchDialogBuilder == null) {
				mSearchDialogBuilder = new UnitSearchDialogBuilder(mCalc.getUnitTypeList());
			}

			mSearchDialogBuilder.buildDialog(mAppContext, mIdlingResource,
					  new AdapterView.OnItemClickListener() {
						  @Override
						  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							  mSearchDialogBuilder.cancelDialog();
							  UnitSearchItem item = mSearchDialogBuilder.getItem(position);
							  selectUnitAtUnitArrayPos(item.getUnitPosition(), item.getUnitTypeKey());
						  }
					  });
		} else if (id == R.id.nav_settings){
			Intent intent = new Intent(mAppContext, SettingsActivity.class);
			startActivity(intent);
		} else if (id == R.id.nav_about){
			PackageInfo pInfo = null;
			String version = "unknown";
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				version = pInfo.versionName;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}

			new AlertDialog.Builder(mAppContext)
					  .setTitle(getText(R.string.about_title))
					  .setMessage(getText(R.string.about_version) + version +
								 "\n\n" + getText(R.string.about_message))
					  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int which) {
							  // continue with delete
						  }
					  })
					  .show();
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)){
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			Calculator.getCalculator(this).saveState();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		//maybe fixes that random crash?
		if (mCalc == null)
			return;

		//load in Unit Type arrangement prefs
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> selections = sharedPref.getStringSet(
				  SettingsActivity.UNIT_TYPE_PREF_KEY, null);

		// determine if user changed the configuration of the Unit Types
		mCalc.setSelectedUnitTypes(selections);

		setupUnitTypePager();

		// if the Unit Type configuration changed, update tab indicator accordingly
//		if (selectionChanged)

		if (mCalc.getCurrUnitType().containsDynamicUnits())
			mCalc.refreshAllDynamicUnits(false);

		//only set display to Equate if no expression is there yet
		if (mCalc.toString().equals("") && mCalc.getResultList().size() == 0){
			mDisplay.setText(R.string.app_name);
			mDisplay.setCursorVisible(false);
		} else {
			updateScreen(true, true);
			mDisplay.setSelectionToEnd();
			//pull ListFrag's focus, to be sure EditText's cursor blinks when app starts
			mDisplay.requestFocus();
		}

	}


	public enum UnitVisibility {VISIBLE, HIDDEN, TOGGLE}

	/**
	 * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
	 */
	@VisibleForTesting
	@NonNull
	public SimpleIdlingResource getIdlingResource() {
		if (mIdlingResource == null) {
			mIdlingResource = new SimpleIdlingResource();
		}
		return mIdlingResource;
	}
}
