package com.iyaffle.launchreview;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import java.util.List;

/**
 * LaunchReviewPlugin
 */
public class LaunchReviewPlugin implements MethodCallHandler {

    private final Registrar mRegistrar;

    private LaunchReviewPlugin(Registrar registrar) {
        this.mRegistrar = registrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "launch_review");
        LaunchReviewPlugin instance = new LaunchReviewPlugin(registrar);
        channel.setMethodCallHandler(instance);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("launch")) {
            String appId = call.argument("android_id");

            if (appId == null) {
                appId = mRegistrar.activity().getPackageName();
            }

            Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appId));
            boolean marketFound = false;

            // find all applications able to handle our rateIntent
            final List<ResolveInfo> otherApps = mRegistrar.activity().getPackageManager()
                    .queryIntentActivities(rateIntent, 0);
            for (ResolveInfo otherApp : otherApps) {
                // look for Google Play application
                if (otherApp.activityInfo.applicationInfo.packageName
                        .equals("com.android.vending")) {

                    ActivityInfo otherAppActivity = otherApp.activityInfo;
                    ComponentName componentName = new ComponentName(
                            otherAppActivity.applicationInfo.packageName,
                            otherAppActivity.name
                    );
                    // make sure it does NOT open in the stack of your activity
                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // task reparenting if needed
                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    // if the Google Play was already open in a search result
                    //  this make sure it still go to the app page you requested
                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // this make sure only the Google Play app is allowed to
                    // intercept the intent
                    rateIntent.setComponent(componentName);
                    mRegistrar.activity().startActivity(rateIntent);
                    marketFound = true;
                    break;

                }
            }

            if (marketFound) {
                result.success("success");
            } else {
                result.success("fail");
            }
        } else {
            result.notImplemented();
        }
    }
}
