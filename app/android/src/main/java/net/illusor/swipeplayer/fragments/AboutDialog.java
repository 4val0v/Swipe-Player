/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import net.illusor.swipeplayer.R;

/**
 * Displays the general information about the application
 */
public class AboutDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Resources res = this.getActivity().getResources();
        String versionString = res.getString(R.string.str_about_version);
        String buildString = res.getString(R.string.str_about_build);

        Pair<String, String> apkInfo = this.getApkInfo();
        Pair<String, String> versionInfo = this.getVersionInfo(apkInfo.second);

        Dialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle(apkInfo.first)
                .setMessage(String.format("%s %s\r\n%s %s", versionString, versionInfo.first, buildString, versionInfo.second))
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

    /**
     * Gets the general information about the app
     * @return Pair of strings (app name - e.g. "Swipe Player", version info - version:build)
     */
    private Pair<String, String> getApkInfo()
    {
        try
        {
            Context context = this.getActivity();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String name = packageInfo.applicationInfo.nonLocalizedLabel.toString();
            String version = packageInfo.versionName;//manifest contains string looking like version@build
            return new Pair<>(name, version);
        }
        catch (PackageManager.NameNotFoundException ignore)
        {
        }

        return new Pair<>("Unknown", "Unknown");
    }

    /**
     * Parses version info
     * @param manifestVersionName VersionName from manifest (version:build string)
     * @return Pair of strings (version, build number)
     */
    private Pair<String, String> getVersionInfo(String manifestVersionName)
    {
        int separator = manifestVersionName.indexOf(":");
        String version = manifestVersionName.substring(0, separator);
        String build = manifestVersionName.substring(separator + 1, manifestVersionName.length());
        return new Pair<>(version, build);
    }
}
