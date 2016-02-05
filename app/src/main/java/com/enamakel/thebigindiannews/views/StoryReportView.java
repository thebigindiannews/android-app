package com.enamakel.thebigindiannews.views;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.models.ReportModel;
import com.enamakel.thebigindiannews.data.models.StoryModel;


public class StoryReportView extends RelativeLayout implements
        View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {
    final AlertDialog dialog;
    StoryModel story;
    RadioGroup radioGroup;
    EditText reportReason;
    Context context;


    public StoryReportView(Context context, StoryModel story, final AlertDialog dialog) {
        super(context);
        inflate(context, R.layout.story_report_view, this);

        this.context = context;
        this.story = story;
        this.dialog = dialog;

        initializeViews();
        initializeDialog();
    }


    /**
     * Helper function to initialize the view components.
     */
    void initializeViews() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_report_group);
        reportReason = (EditText) findViewById(R.id.report_reason);

        reportReason.setVisibility(GONE);
        radioGroup.setOnCheckedChangeListener(this);
    }


    /**
     * Helper function to attach the view to the dialog.
     */
    void initializeDialog() {
        // Attach this view to the dialog
        dialog.setView(this);

        // Override the on click listener!
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                // Override the positive button
                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(StoryReportView.this);
            }
        });
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio_others) reportReason.setVisibility(VISIBLE);
        else reportReason.setVisibility(GONE);
    }


    @Override
    public void onClick(View v) {
        ReportModel report = new ReportModel();

        switch (radioGroup.getCheckedRadioButtonId()) {
            case -1:
                Toast.makeText(context, "You need to choose at least one optio", Toast.LENGTH_LONG)
                        .show();
                break;
            case R.id.radio_confidential:
                report.setType(ReportModel.Type.CONFIDENTIAL);
                submit(report);
                break;

            case R.id.radio_irrelevant:
                report.setType(ReportModel.Type.IRRELEVANT);
                submit(report);
                break;

            case R.id.radio_junk:
                report.setType(ReportModel.Type.JUNK);
                submit(report);
                break;

            case R.id.radio_language:
                report.setType(ReportModel.Type.BAD_LANGUAGE);
                submit(report);
                break;

            case R.id.radio_vulgur:
                report.setType(ReportModel.Type.VULGUR);
                submit(report);
                break;

            case R.id.radio_others:
                String reason = reportReason.getText().toString();
                if (reason.isEmpty()) {
                    Toast.makeText(context, "You need to give a report reason", Toast.LENGTH_LONG)
                            .show();
                } else {
                    report.setType(ReportModel.Type.OTHER);
                    report.setReason(reason);
                    submit(report);
                    break;
                }
                break;
        }
    }


    /**
     * Helper function to submit a report to the server. This function should be called with all
     * the validation done before hand.
     *
     * @param report The {@link ReportModel} to send to the server.
     */
    void submit(ReportModel report) {
        // Set the story id in the report.
        report.setStory(story.getId());

        // send to server

        // Notify the user
        Toast.makeText(context, "Thank you, Your report has been sent!", Toast.LENGTH_LONG)
                .show();
        dialog.dismiss();
    }


    /**
     * Creates a dialog with the report screen.
     *
     * @param context Context of the application.
     * @param story   Story data to create the view around.
     * @return An {@link AlertDialog} instance which is ready to be displayed to the user.
     */
    public static AlertDialog buildDialog(Context context, StoryModel story) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.report_dialog_title)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //All of the fun happens inside the CustomListener now.
                        //I had to move it to enable data validation.
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Attach the report view to the dialog.
        StoryReportView.attachToDialog(context, story, dialog);

        return dialog;
    }


    /**
     * Attach a StoryReportView to the given dialog.
     *
     * @param context Context of the application.
     * @param story   Story data to create the view around.
     * @param dialog  The dialog to put the view in.
     */
    public static void attachToDialog(Context context, StoryModel story, AlertDialog dialog) {
        new StoryReportView(context, story, dialog);
    }
}