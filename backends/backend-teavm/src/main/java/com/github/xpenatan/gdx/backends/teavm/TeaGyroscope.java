package com.github.xpenatan.gdx.backends.teavm;

/**
 * Port from GWT gdx
 *
 * @author xpenatan
 */
public abstract class TeaGyroscope extends TeaSensor {

    public static final String PERMISSION = "gyroscope";

    protected TeaGyroscope() {
    }

    static native TeaGyroscope getInstance() /*-{
		return new $wnd.Gyroscope();
	}-*/;

    static native boolean isSupported() /*-{
		return "Gyroscope" in $wnd;
	}-*/;

    final native double x() /*-{
		return this.x ? this.x : 0;
	}-*/;

    final native double y() /*-{
		return this.y ? this.y : 0;
	}-*/;

    final native double z() /*-{
		return this.z ? this.z : 0;
	}-*/;
}
