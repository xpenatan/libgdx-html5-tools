package com.github.xpenatan.gdx.backends.teavm;


/**
 * @author xpenatan
 */
public class TeaApplicationConfiguration {

    /**
     * Use the new updated port from GWT or the old implementation. The old will be deleted soon.
     */
    @Deprecated()
    boolean useUpdatedInputClass = true;

    /** Experimental, whether to enable OpenGL ES 30 (aka WebGL2) if supported. If not supported it will fall-back to OpenGL ES
     * 2.0. When GL ES 30 is enabled, {@link com.badlogic.gdx.Gdx#gl30} can be used to access its functionality.
     * @deprecated this option is currently experimental and not yet fully supported, expect issues. */
    @Deprecated
    public boolean useGL30 = false;

    /** Sets the {@link TeaWindowListener} which will be informed about teavm events. */
    public TeaWindowListener windowListener;

    /**
     * Load assets before starting the game. For custom loading, change to false.
     */
    public boolean preloadAssets = true;

    /**
     * The prefix for the browser storage. If you have multiple apps on the same server and want to keep the
     * data separate for those applications, you will need to set unique prefixes. This is useful if you are
     * e.g. uploading multiple webapps to itch.io and want to keep the data separate for each application.
     * <p>
     * For example use "app1_" for one, and "app2_" for the other application, so the data that is stored in the
     * browser is not shared between the applications. If you leave the storage prefix at "", all the data
     * and files stored will be shared between the applications.
     */
    public String storagePrefix = "app";

    public String localStoragePrefix = "db/assets";

    public boolean shouldEncodePreference = false;

    /**
     * Show download logs.
     */
    public boolean showDownloadLogs = false;

    public String canvasID;

    /**
     * the width of the drawing area in pixels, 0 for using the available space or -1 to use html canvas size
     **/
    public int width = -1;
    /**
     * the height of the drawing area in pixels, 0 for using the available space or -1 to use html canvas size
     **/
    public int height = -1;
    /**
     * Padding to use for resizing the game content in the browser window, for resizable applications only. Defaults to 0. The
     * padding is necessary to prevent the browser from showing scrollbars. This can happen if the game content is of the same size
     * than the browser window. The padding is given in logical pixels, not affected by {@link #usePhysicalPixels}.
     */
    public int padHorizontal = 0, padVertical = 0;
    /**
     * whether to use a stencil buffer
     **/
    public boolean stencil = false;
    /**
     * whether to enable antialiasing
     **/
    public boolean antialiasing = false;
    /**
     * whether to include an alpha channel in the color buffer
     **/
    public boolean alpha = false;
    /**
     * whether to use premultipliedalpha, may have performance impact
     **/
    public boolean premultipliedAlpha = false;
    /**
     * preserve the back buffer, needed if you fetch a screenshot via canvas#toDataUrl, may have performance impact
     **/
    public boolean preserveDrawingBuffer = false;
    /**
     * whether to use debugging mode for OpenGL calls. Errors will result in a RuntimeException being thrown.
     */
    public boolean useDebugGL = false;

    /** Whether to use physical device pixels or CSS pixels for scaling the canvas. Makes a difference on mobile devices and HDPI
     * and Retina displays. Set to true for resizable and fullscreen games on mobile devices and for Desktops if you want to use
     * the full resolution of HDPI/Retina displays.<br/>
     * Setting to false mostly makes sense for fixed-size games or non-mobile games expecting performance issues on huge
     * resolutions. If you target mobiles and desktops, consider using physical device pixels on mobile devices only by using the
     * return value of {@link TeaApplication#isMobileDevice()} . */
    public boolean usePhysicalPixels = false;

    /**
     * default, low-power or high-performance
     */
    public String powerPreference = "high-performance";

    /**
     * Used for preloading asset and libraries
     */
    public TeaAssetPreloadListener preloadListener;

    /** whether to use the accelerometer. default: true **/
    public boolean useAccelerometer = true;
    /** whether to use the gyroscope. default: false **/
    public boolean useGyroscope = false;

    public boolean isFixedSizeApplication() {
        return width != 0 && height != 0;
    }

    public boolean isAutoSizeApplication() {
        return width == 0 && height == 0;
    }

    public TeaApplicationConfiguration(String canvasID) {
        this.canvasID = canvasID;
    }
}