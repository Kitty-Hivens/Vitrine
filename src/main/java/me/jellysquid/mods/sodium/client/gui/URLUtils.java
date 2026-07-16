package me.jellysquid.mods.sodium.client.gui;

import dev.hivens.vitrine.Vitrine;
import net.minecraft.util.Util;

import java.io.IOException;

public class URLUtils {

    private static String[] getURLOpenCommand(String url) {
        switch (Util.getOSType()) {
            case WINDOWS:
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
            case OSX:
                return new String[]{"open", url};
            case UNKNOWN:
            case LINUX:
            case SOLARIS:
                return new String[]{"xdg-open", url};
            default:
                throw new IllegalArgumentException("Unexpected OS Type");
        }
    }

    public static void open(String url) {
        try {
            Runtime.getRuntime().exec(getURLOpenCommand(url));
        } catch (IOException exception) {
            Vitrine.logger().error("Couldn't open url '{}'", url, exception);
        }

    }

}