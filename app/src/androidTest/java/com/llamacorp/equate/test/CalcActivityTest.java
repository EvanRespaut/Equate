package com.llamacorp.equate.test;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.view.View;

import com.llamacorp.equate.R;
import com.llamacorp.equate.view.CalcActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.llamacorp.equate.test.IsEqualTrimmingAndIgnoringCase.equalToTrimmingAndIgnoringCase;
import static com.llamacorp.equate.test.VisibleViewMatcher.isVisible;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalcActivityTest {

  @Rule
  public ActivityTestRule<CalcActivity> mActivityTestRule =
      new ActivityTestRule<>(CalcActivity.class);

  @Test
  public void testCalcActivity() {
    Espresso.pressBack();

    ViewInteraction android_widget_TextView =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Speed")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView.perform(click());

    ViewInteraction android_widget_TextView2 =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Time")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView2.perform(getClickAction());

    ViewInteraction android_widget_Button =
        onView(
            allOf(
                withId(R.id.one_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("1")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button.perform(getClickAction());

    ViewInteraction android_widget_Button2 =
        onView(
            allOf(
                withId(R.id.convert_button4),
                withTextOrHint(equalToTrimmingAndIgnoringCase("DAY")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button2.perform(getClickAction());

    ViewInteraction android_widget_Button3 =
        onView(
            allOf(
                withId(R.id.equals_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("=")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button3.perform(getClickAction());

    Espresso.pressBack();

    ViewInteraction android_widget_Button4 =
        onView(
            allOf(
                withId(R.id.close_para_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase(")")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button4.perform(getClickAction());

    ViewInteraction android_widget_Button5 =
        onView(
            allOf(
                withId(R.id.decimal_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase(".")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button5.perform(getClickAction());

    ViewInteraction android_widget_Button6 =
        onView(
            allOf(
                withId(R.id.convert_button1),
                withTextOrHint(equalToTrimmingAndIgnoringCase("➜ SEC")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button6.perform(getClickAction());

    ViewInteraction android_widget_Button7 =
        onView(
            allOf(
                withId(R.id.seven_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("7")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button7.perform(getClickAction());

    ViewInteraction android_widget_Button8 =
        onView(
            allOf(
                withId(R.id.decimal_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase(".")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button8.perform(getLongClickAction());

    ViewInteraction android_widget_TextView3 =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Time")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView3.perform(getClickAction());

    ViewInteraction android_widget_Button9 =
        onView(
            allOf(
                withId(R.id.convert_button3),
                withTextOrHint(equalToTrimmingAndIgnoringCase("HOUR")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button9.perform(getClickAction());

    ViewInteraction android_widget_Button10 =
        onView(
            allOf(
                withId(R.id.divide_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("÷")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button10.perform(getClickAction());

    ViewInteraction android_widget_Button11 =
        onView(
            allOf(
                withId(R.id.zero_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("0")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button11.perform(getLongClickAction());

    ViewInteraction root = onView(isRoot());
    root.perform(getSwipeAction(540, 759, 240, 759));

    waitToScrollEnd();

    ViewInteraction android_widget_Button12 =
        onView(
            allOf(
                withId(R.id.equals_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("=")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button12.perform(getClickAction());

    ViewInteraction root2 = onView(isRoot());
    root2.perform(getSwipeAction(540, 759, 840, 759));

    waitToScrollEnd();

    ViewInteraction android_widget_Button13 =
        onView(
            allOf(
                withId(R.id.five_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("5")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button13.perform(getLongClickAction());

    ViewInteraction android_widget_TextView4 =
        onView(
            allOf(
                withId(R.id.list_item_result_textPrevQuery),
                withTextOrHint(equalToTrimmingAndIgnoringCase("7. sec")),
                isVisible()));
    android_widget_TextView4.perform(getLongClickAction());

    ViewInteraction android_widget_Button14 =
        onView(
            allOf(
                withId(R.id.eight_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("8")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button14.perform(getClickAction());

    ViewInteraction android_widget_TextView5 =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Volume")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView5.perform(getClickAction());

    ViewInteraction android_widget_Button15 =
        onView(
            allOf(
                withId(R.id.three_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("3")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button15.perform(getLongClickAction());

    ViewInteraction root3 = onView(isRoot());
    root3.perform(getSwipeAction(540, 759, 540, 1059));

    waitToScrollEnd();

    ViewInteraction android_widget_TextView6 =
        onView(
            allOf(
                withId(R.id.resultPreview),
                withTextOrHint(equalToTrimmingAndIgnoringCase("= 583")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.resultPreviewContainer),
                        isDescendantOfA(withId(R.id.drawer_layout))))));
    android_widget_TextView6.perform(getClickAction());

    ViewInteraction android_widget_Button16 =
        onView(
            allOf(
                withId(R.id.four_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("4")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button16.perform(getLongClickAction());

    ViewInteraction root4 = onView(isRoot());
    root4.perform(getSwipeAction(540, 759, 240, 759));

    waitToScrollEnd();

    ViewInteraction root5 = onView(isRoot());
    root5.perform(getSwipeAction(540, 897, 0, 897));

    waitToScrollEnd();

    ViewInteraction android_widget_Button17 =
        onView(
            allOf(
                withId(R.id.convert_button4),
                withTextOrHint(equalToTrimmingAndIgnoringCase("QT")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button17.perform(getClickAction());

    ViewInteraction android_widget_Button18 =
        onView(
            allOf(
                withId(R.id.convert_button8),
                withTextOrHint(equalToTrimmingAndIgnoringCase("➜ ML")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button18.perform(getClickAction());

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction android_widget_Button19 =
        onView(
            allOf(
                withId(R.id.convert_button2),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CUP")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button19.perform(getLongClickAction());

    Espresso.pressBack();

    ViewInteraction android_widget_Button20 =
        onView(
            allOf(
                withId(R.id.convert_button2),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CUP")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button20.perform(getLongClickAction());

    Espresso.pressBack();

    ViewInteraction root6 = onView(isRoot());
    root6.perform(getSwipeAction(540, 759, 540, 1059));

    waitToScrollEnd();

    ViewInteraction android_widget_TextView7 =
        onView(
            allOf(
                withId(R.id.list_item_result_textPrevAnswer),
                withTextOrHint(equalToTrimmingAndIgnoringCase("Divide By Zero Error")),
                isVisible()));
    android_widget_TextView7.perform(getLongClickAction());

    ViewInteraction android_widget_ImageButton =
        onView(
            allOf(
                withId(R.id.backspace_button),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_ImageButton.perform(getClickAction());

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction android_widget_Button21 =
        onView(
            allOf(
                withId(R.id.clear_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("C")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button21.perform(getClickAction());

    ViewInteraction root7 = onView(isRoot());
    root7.perform(getSwipeAction(540, 759, 840, 759));

    waitToScrollEnd();

    ViewInteraction android_support_v4_view_ViewPager =
        onView(
            allOf(
                withId(R.id.unit_pager),
                isVisible(),
                hasDescendant(
                    allOf(
                        withId(R.id.convert_button1),
                        withTextOrHint(equalToTrimmingAndIgnoringCase("TBSP")))),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_container),
                        isDescendantOfA(withId(R.id.drawer_layout))))));
    android_support_v4_view_ViewPager.perform(getClickAction());

    ViewInteraction android_widget_Button22 =
        onView(
            allOf(
                withId(R.id.convert_button9),
                withTextOrHint(equalToTrimmingAndIgnoringCase("L")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_pager),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_Button22.perform(getLongClickAction());

    Espresso.pressBack();

    ViewInteraction android_widget_TextView8 =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Area")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView8.perform(getClickAction());

    ViewInteraction android_widget_TextView9 =
        onView(
            allOf(
                withTextOrHint(equalToTrimmingAndIgnoringCase("Speed")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.unit_type_titles),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.unit_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_TextView9.perform(getClickAction());

    ViewInteraction android_widget_Button23 =
        onView(
            allOf(
                withId(R.id.eight_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("8")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button23.perform(getLongClickAction());

    ViewInteraction android_widget_Button24 =
        onView(
            allOf(
                withId(R.id.clear_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("C")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button24.perform(getClickAction());

    ViewInteraction android_widget_Button25 =
        onView(
            allOf(
                withId(R.id.nine_button),
                withTextOrHint(equalToTrimmingAndIgnoringCase("9")),
                isVisible(),
                isDescendantOfA(withId(R.id.drawer_layout))));
    android_widget_Button25.perform(getClickAction());
  }

  private static Matcher<View> withTextOrHint(final Matcher<String> stringMatcher) {
    return anyOf(withText(stringMatcher), withHint(stringMatcher));
  }

  private ViewAction getSwipeAction(
      final int fromX, final int fromY, final int toX, final int toY) {
    return ViewActions.actionWithAssertions(
        new GeneralSwipeAction(
            Swipe.SLOW,
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                float[] coordinates = {fromX, fromY};
                return coordinates;
              }
            },
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                float[] coordinates = {toX, toY};
                return coordinates;
              }
            },
            Press.FINGER));
  }

  private void waitToScrollEnd() {
    SystemClock.sleep(500);
  }

  private ClickWithoutDisplayConstraint getClickAction() {
    return new ClickWithoutDisplayConstraint(
        Tap.SINGLE,
        GeneralLocation.VISIBLE_CENTER,
        Press.FINGER);
  }

  private ClickWithoutDisplayConstraint getLongClickAction() {
    return new ClickWithoutDisplayConstraint(
        Tap.LONG,
        GeneralLocation.CENTER,
        Press.FINGER);
  }
}
