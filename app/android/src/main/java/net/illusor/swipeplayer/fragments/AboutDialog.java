package net.illusor.swipeplayer.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import net.illusor.swipeplayer.R;

public class AboutDialog extends DialogFragment
{
    private static final String PARAM_NAME = "name";
    private static final String PARAM_VERSION = "version";

    public static AboutDialog newInstance(String appName, String appVersion)
    {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_NAME, appName);
        bundle.putSerializable(PARAM_VERSION, appVersion);
        AboutDialog dialog = new AboutDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        String name = bundle.getString(PARAM_NAME);
        String version = bundle.getString(PARAM_VERSION);
        String message = this.getActivity().getResources().getString(R.string.str_about_version);

        Dialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle(name)
                .setMessage(String.format("%s %s", message, version))
                .setNeutralButton(R.string.str_about_close, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int i)
                    {
                        dialog.dismiss();
                    }
                }).create();

        return dialog;
    }
}
