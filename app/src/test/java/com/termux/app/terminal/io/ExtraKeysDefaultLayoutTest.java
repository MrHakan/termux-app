package com.termux.app.terminal.io;

import com.termux.shared.termux.extrakeys.ExtraKeyButton;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.extrakeys.ExtraKeysInfo;
import com.termux.shared.termux.settings.properties.TermuxPropertyConstants;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for the built-in extra keys layout.
 *
 * A broken default matters more than a broken user layout: when the layout from
 * termux.properties fails to parse, TermuxTerminalExtraKeys#setExtraKeys() falls back to
 * {@link TermuxPropertyConstants#DEFAULT_IVALUE_EXTRA_KEYS}, so if that string is itself
 * invalid there is nothing left to fall back to and the extra keys row disappears entirely.
 *
 * Runs under Robolectric because the parsing goes through Android's org.json, which unlike
 * the reference implementation accepts the unquoted keys and single quotes the layout uses.
 */
@RunWith(RobolectricTestRunner.class)
public class ExtraKeysDefaultLayoutTest {

    private static ExtraKeyButton[][] parseDefaultLayout() throws Exception {
        return new ExtraKeysInfo(TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS,
            TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE,
            ExtraKeysConstants.CONTROL_CHARS_ALIASES).getMatrix();
    }

    private static ExtraKeyButton findButton(ExtraKeyButton[][] matrix, String key) {
        for (ExtraKeyButton[] row : matrix) {
            for (ExtraKeyButton button : row) {
                if (key.equals(button.getKey())) return button;
            }
        }
        return null;
    }

    @Test
    public void defaultLayoutParses() throws Exception {
        Assert.assertNotNull(parseDefaultLayout());
    }

    /**
     * ExtraKeysView pins every button to a fixed column index and sizes the grid to the longest
     * row, so a shorter row renders with an empty gap at its end rather than stretching to fit.
     */
    @Test
    public void defaultLayoutRowsAreEqualLength() throws Exception {
        ExtraKeyButton[][] matrix = parseDefaultLayout();
        for (ExtraKeyButton[] row : matrix) {
            Assert.assertEquals("every row must have the same number of keys",
                matrix[0].length, row.length);
        }
    }

    @Test
    public void sessionKeysArePresentWithTheirDisplayText() throws Exception {
        ExtraKeyButton[][] matrix = parseDefaultLayout();

        ExtraKeyButton nextSession = findButton(matrix, "NEXT_SESSION");
        Assert.assertNotNull("NEXT_SESSION must be in the default layout", nextSession);
        Assert.assertEquals("⇄", nextSession.getDisplay());

        ExtraKeyButton newSession = findButton(matrix, "NEW_SESSION");
        Assert.assertNotNull("NEW_SESSION must be in the default layout", newSession);
        Assert.assertEquals("＋", newSession.getDisplay());
    }

    /** Swiping up on the session switch key opens the drawer with the full session list. */
    @Test
    public void nextSessionKeyHasDrawerPopup() throws Exception {
        ExtraKeyButton nextSession = findButton(parseDefaultLayout(), "NEXT_SESSION");
        Assert.assertNotNull(nextSession);
        Assert.assertNotNull("NEXT_SESSION must have a popup", nextSession.getPopup());
        Assert.assertEquals("DRAWER", nextSession.getPopup().getKey());
    }

}
