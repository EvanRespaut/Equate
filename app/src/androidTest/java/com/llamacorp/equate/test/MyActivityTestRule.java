package com.llamacorp.equate.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;

import com.llamacorp.equate.view.CalcActivity;

import org.hamcrest.Matcher;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * This class was created to allow for custom setup and tear down code before
 * and after the activity runs. This includes a custom error handler that can
 * perform additional duties besides printing the stack trace (maybe take a
 * screenshot)
 */
class MyActivityTestRule<A extends CalcActivity> extends ActivityTestRule<A> {
	public MyActivityTestRule(Class<A> activityClass) {
		super(activityClass);
	}

	public MyActivityTestRule(Class<A> activityClass, boolean initialTouchMode) {
		super(activityClass, initialTouchMode);
	}

	public MyActivityTestRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
		super(activityClass, initialTouchMode, launchActivity);
	}

	/**
	 * Override this method to execute any code that should run before your {@link Activity} is
	 * created and launched.
	 * This method is called before each test method, including any method annotated with
	 * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
	 */
	@Override
	protected void beforeActivityLaunched() {
		super.beforeActivityLaunched();

		resetSharedPrefs();
//		setFailureHandler(new CustomFailureHandle(getInstrumentation().getTargetContext()));
	}



	/**
	 * Override this method to execute any code that should run after your {@link Activity} is
	 * launched, but before any test code is run including any method annotated with
	 * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
	 * <p>
	 * Prefer
	 * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>
	 * over this method. This method should usually not be overwritten directly in tests and only be
	 * used by subclasses of ActivityTestRule to get notified when the activity is created and
	 * visible but test runs.
	 */
	@Override
	protected void afterActivityLaunched() {
		super.afterActivityLaunched();

	}


	private void resetSharedPrefs() {
		File root = getTargetContext().getFilesDir().getParentFile();
		String[] sharedPreferencesFileNames = new File(root, "shared_prefs").list();
		if (sharedPreferencesFileNames == null) return;
		for (String fileName : sharedPreferencesFileNames) {
			SharedPreferences sp = InstrumentationRegistry.getTargetContext()
					  .getSharedPreferences(fileName.replace(".xml", ""), Context.MODE_PRIVATE);
			sp.edit().clear().apply();
			File fileToDelete = new File(root + "/shared_prefs/" + fileName);
			boolean wasSuccessful = fileToDelete.delete();
			Log.d("ESPRESSO_LOG", "File deleted = " + String.valueOf(wasSuccessful));
		}
	}


	private static class CustomFailureHandle implements FailureHandler {
		private final FailureHandler delegate;

		public CustomFailureHandle(Context targetContext) {
			delegate = new DefaultFailureHandler(targetContext);
		}

		/**
		 * Handle the given error in a manner that makes sense to the environment
		 * in which the test is executed (e.g. take a screenshot, output extra
		 * debug info, etc). Upon handling, most handlers will choose to propagate
		 * the error.
		 *
		 * @param error
		 * @param viewMatcher
		 */
		@Override
		public void handle(Throwable error, Matcher<View> viewMatcher) {
			try {
				delegate.handle(error, viewMatcher);
			} catch (NoMatchingViewException e) {
				throw new MySpecialException(e);
			}
		}

		private static class MySpecialException extends RuntimeException {
			MySpecialException(Throwable cause) {
				super(cause);
			}
		}
	}
}
