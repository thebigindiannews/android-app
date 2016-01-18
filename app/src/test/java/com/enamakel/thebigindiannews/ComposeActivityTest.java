package com.enamakel.thebigindiannews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;

import com.enamakel.thebigindiannews.accounts.UserServices;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class ComposeActivityTest {
    private ActivityController<ComposeActivity> controller;
    private ComposeActivity activity;
    @Inject UserServices userServices;
    @Captor ArgumentCaptor<UserServices.Callback> replyCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestApplication.applicationGraph.inject(this);
        reset(userServices);
        controller = Robolectric.buildActivity(ComposeActivity.class);
        activity = controller.get();
        Intent intent = new Intent();
        intent.putExtra(ComposeActivity.EXTRA_PARENT_ID, "1");
        intent.putExtra(ComposeActivity.EXTRA_PARENT_TEXT, "Paragraph 1<br/><br/>Paragraph 2<br/>");
        controller.withIntent(intent).create().start().resume().visible();
    }

    @Test
    public void testNoId() {
        controller = Robolectric.buildActivity(ComposeActivity.class)
                .create().start().resume().visible();
        activity = controller.get();
        assertThat(activity).isFinishing();
    }

    @Test
    public void testToggle() {
        assertThat(activity.findViewById(R.id.toggle)).isVisible();
        assertThat(activity.findViewById(R.id.text)).isNotVisible();
        activity.findViewById(R.id.toggle).performClick();
        assertThat(activity.findViewById(R.id.text)).isVisible();
        assertThat((TextView) activity.findViewById(R.id.text))
                .hasTextString("Paragraph 1\n\nParagraph 2");
        activity.findViewById(R.id.toggle).performClick();
        assertThat(activity.findViewById(R.id.text)).isNotVisible();
    }

    @Test
    public void testHomeButtonClick() {
        shadowOf(activity).clickMenuItem(android.R.id.home);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testConfirmExit() {
        ((EditText) activity.findViewById(R.id.edittext_body)).setText("Reply");
        shadowOf(activity).clickMenuItem(android.R.id.home);
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertThat(activity).isFinishing();
    }

    @Test
    public void testSendEmpty() {
        shadowOf(activity).clickMenuItem(R.id.menu_send);
        assertEquals(activity.getString(R.string.comment_required), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testQuote() {
        assertTrue(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_quote).isVisible());
        ((EditText) activity.findViewById(R.id.edittext_body)).setText("Reply");
        shadowOf(activity).clickMenuItem(R.id.menu_quote);
        assertThat((EditText) activity.findViewById(R.id.edittext_body))
                .hasTextString("> Paragraph 1\n\n> Paragraph 2\n\nReply");
    }

    @Test
    public void testClickEmptyFocusEditText() {
        View editText = activity.findViewById(R.id.edittext_body);
        editText.clearFocus();
        assertThat(editText).isNotFocused();
        activity.findViewById(R.id.empty).performClick();
        assertThat(editText).isFocused();
    }

    @Test
    public void testGuidelines() {
        activity.findViewById(R.id.guidelines).performClick();
        assertNotNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void testEmptyQuote() {
        Intent intent = new Intent();
        intent.putExtra(ComposeActivity.EXTRA_PARENT_ID, "1");
        controller = Robolectric.buildActivity(ComposeActivity.class)
                .withIntent(intent).create().start().resume().visible();
        activity = controller.get();
        assertThat(activity.findViewById(R.id.toggle)).isNotVisible();
        assertFalse(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_quote).isVisible());
    }

    @Test
    public void testSendPromptToLogin() {
        doSend();
        replyCallback.getValue().onDone(false);
        assertThat(shadowOf(activity).getNextStartedActivity())
                .hasComponent(activity, LoginActivity.class);
    }

    @Test
    public void testSendSuccessful() {
        doSend();
        replyCallback.getValue().onDone(true);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testSendFailed() {
        doSend();
        assertFalse(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_send).isEnabled());
        assertFalse(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_quote).isVisible());
        replyCallback.getValue().onError();
        assertTrue(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_send).isEnabled());
        assertTrue(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_quote).isVisible());
        assertThat(activity).isNotFinishing();
        assertEquals(activity.getString(R.string.comment_failed), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testDelayedSuccessfulResponse() {
        doSend();
        shadowOf(activity).clickMenuItem(android.R.id.home);
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        assertEquals(activity.getString(R.string.confirm_no_waiting), shadowOf(alertDialog).getMessage());
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertThat(activity).isFinishing();
        replyCallback.getValue().onDone(true);
        assertEquals(activity.getString(R.string.comment_successful), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testDelayedFailedResponse() {
        doSend();
        shadowOf(activity).clickMenuItem(android.R.id.home);
        ShadowAlertDialog.getLatestAlertDialog()
                .getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        replyCallback.getValue().onDone(false);
        assertNull(shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void testDelayedError() {
        doSend();
        shadowOf(activity).clickMenuItem(android.R.id.home);
        ShadowAlertDialog.getLatestAlertDialog()
                .getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        replyCallback.getValue().onError();
        assertEquals(activity.getString(R.string.comment_failed), ShadowToast.getTextOfLatestToast());
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }

    private void doSend() {
        ((EditText) activity.findViewById(R.id.edittext_body)).setText("Reply");
        shadowOf(activity).clickMenuItem(R.id.menu_send);
        verify(userServices).reply(any(Context.class), eq("1"), eq("Reply"), replyCallback.capture());
    }
}
