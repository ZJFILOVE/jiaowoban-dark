package io.github.jiaowoban.dark;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextContrastTest {
    @Test public void labelHasReadableContrastOnEmptyStateSurface() {
        int text = TextContrast.forDarkBackground(0xff333333, true);
        double ratio = (luminance(text) + .05) / (luminance(0xff0c0d10) + .05);
        assertTrue("Normal-sized label needs WCAG AA contrast", ratio >= 4.5);
    }
    @Test public void accentAndWhiteHeaderTextArePreserved() {
        assertEquals(0xff0066ee, TextContrast.forDarkBackground(0xff0066ee, true));
        assertEquals(0xffffffff, TextContrast.forDarkBackground(0xffffffff, true));
        assertEquals(0xffff3333, TextContrast.forDarkBackground(0xffff3333, true));
    }
    @Test public void transparentAndDisabledStatesKeepTheirOpacity() {
        assertEquals(0x00333333, TextContrast.forDarkBackground(0x00333333, true));
        assertEquals(0x66, TextContrast.forDarkBackground(0x66333333, false) >>> 24);
    }
    @Test public void faintEnabledLabelMeetsContrastAfterCompositing() {
        int color = TextContrast.forDarkBackground(0x33000000, true);
        double alpha = (color >>> 24) / 255.0;
        int composited = 0xff000000;
        for (int shift : new int[]{16, 8, 0}) {
            int foreground = (color >>> shift) & 255;
            int background = (0xff0c0d10 >>> shift) & 255;
            composited |= ((int) (foreground * alpha + background * (1-alpha))) << shift;
        }
        assertTrue((luminance(composited) + .05) / (luminance(0xff0c0d10) + .05) >= 4.5);
    }
    private static double luminance(int color) {
        return .2126 * channel(color >> 16) + .7152 * channel(color >> 8) + .0722 * channel(color);
    }
    private static double channel(int value) {
        double c = (value & 255) / 255.0;
        return c <= .04045 ? c / 12.92 : Math.pow((c + .055) / 1.055, 2.4);
    }
}
