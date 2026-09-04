package com.termux.shared.android;

import android.app.PendingIntent;
import android.os.Build;

public class PendingIntentUtils {

    /**
     * Add the appropriate mutability flag to the {@code baseFlags} of a {@link PendingIntent}.
     *
     * A {@link PendingIntent} runs with the identity of the app that created it, so a mutable one
     * handed to another app lets that app fill in the blanks of the wrapped {@link
     * android.content.Intent} and have it sent as Termux. Android 12 (API 31) therefore made
     * declaring the mutability mandatory and throws an {@link IllegalArgumentException} when
     * neither {@link PendingIntent#FLAG_IMMUTABLE} nor {@link PendingIntent#FLAG_MUTABLE} is
     * passed by an app targeting API 31 or higher.
     *
     * Termux still targets API 28 and so is not subject to that check, but requesting immutability
     * is the safer default regardless of the target, and keeps these call sites correct if the
     * target is ever raised.
     *
     * @param baseFlags The flags to add the mutability flag to, like {@link
     *                  PendingIntent#FLAG_UPDATE_CURRENT}.
     * @param mutable Whether the {@link PendingIntent} must stay mutable. Only pass {@code true}
     *                for a {@link PendingIntent} whose wrapped intent is intentionally filled in
     *                by its receiver, like the ones handed to direct reply notification actions.
     * @return Returns the {@code baseFlags} with the mutability flag added.
     */
    public static int getPendingIntentFlags(int baseFlags, boolean mutable) {
        if (mutable) {
            // FLAG_MUTABLE only exists on API 31+, and pending intents are mutable by default below it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                return baseFlags | PendingIntent.FLAG_MUTABLE;
        } else {
            // FLAG_IMMUTABLE was added in API 23.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return baseFlags | PendingIntent.FLAG_IMMUTABLE;
        }

        return baseFlags;
    }

    /** Wrapper for {@link #getPendingIntentFlags(int, boolean)} that requests an immutable intent. */
    public static int getPendingIntentFlags(int baseFlags) {
        return getPendingIntentFlags(baseFlags, false);
    }

}
