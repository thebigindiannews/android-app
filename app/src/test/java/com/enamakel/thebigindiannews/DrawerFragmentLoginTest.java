package com.enamakel.thebigindiannews;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAccountManager;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.support.v4.Shadows;
import org.robolectric.util.ActivityController;

import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;
import com.enamakel.thebigindiannews.test.TestListActivity;

import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowSupportPreferenceManager.class})
@RunWith(RobolectricGradleTestRunner.class)
public class DrawerFragmentLoginTest {
    private ActivityController<TestListActivity> controller;
    private TestListActivity activity;
    private TextView drawerAccount;
    private View drawerLogout;
    private View drawerUser;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(TestListActivity.class)
                .create()
                .postCreate(null)
                .start()
                .resume()
                .visible();
        activity = controller.get();
        drawerAccount = (TextView) activity.findViewById(R.id.drawer_account);
        drawerLogout = activity.findViewById(R.id.drawer_logout);
        drawerUser = activity.findViewById(R.id.drawer_user);
    }

    @Test
    public void testNoExistingAccount() {
        assertThat(drawerAccount).hasText(R.string.login);
        assertThat(drawerLogout).isNotVisible();
        assertThat(drawerUser).isNotVisible();
        Preferences.setUsername(activity, "username");
        assertThat(drawerAccount).hasText("username");
        assertThat(drawerLogout).isVisible();
        assertThat(drawerUser).isVisible();
        drawerLogout.performClick();
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertThat(drawerAccount).hasText(R.string.login);
        assertThat(drawerLogout).isNotVisible();
    }

    @Test
    public void testOpenUserProfile() {
        Preferences.setUsername(activity, "username");
        drawerUser.performClick();
        Shadows.shadowOf((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                .getDrawerListener()
                .onDrawerClosed(activity.findViewById(R.id.drawer));
        assertThat(shadowOf(activity).getNextStartedActivity())
                .hasComponent(activity, UserActivity.class)
                .hasExtra(UserActivity.EXTRA_USERNAME, "username");
    }

    @Test
    public void testExistingAccount() {
        ShadowAccountManager.get(activity).addAccountExplicitly(new Account("existing",
                BuildConfig.APPLICATION_ID), "password", null);
        drawerAccount.performClick();
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        assertThat(alertDialog.getListView().getAdapter()).hasCount(2); // existing + add account
        shadowOf(alertDialog).clickOnItem(0);
        assertThat(alertDialog).isNotShowing();
        assertThat(drawerAccount).hasText("existing");
        assertThat(drawerLogout).isVisible();
        drawerAccount.performClick();
        alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(alertDialog.getListView().getAdapter()).hasCount(2); // existing + add account
    }

    @Test
    public void testAddAccount() {
        ShadowAccountManager.get(activity).addAccountExplicitly(new Account("existing",
                BuildConfig.APPLICATION_ID), "password", null);
        drawerAccount.performClick();
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        assertThat(alertDialog.getListView().getAdapter()).hasCount(2); // existing + add account
        shadowOf(alertDialog).clickOnItem(1);
        assertThat(alertDialog).isNotShowing();
        Shadows.shadowOf((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                .getDrawerListener()
                .onDrawerClosed(activity.findViewById(R.id.drawer));
        assertThat(shadowOf(activity).getNextStartedActivity())
                .hasComponent(activity, LoginActivity.class);
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }
}
