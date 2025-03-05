package com.github.xpenatan.gdx.backends.teavm;

/**
 * Port from GWT gdx
 *
 * @author xpenatan
 */
public abstract class TeaAccelerometer extends TeaSensor {

	public static final String PERMISSION = "accelerometer";

	protected TeaAccelerometer() {
	}

	static native TeaAccelerometer getInstance () /*-{
		return new $wnd.Accelerometer();
	}-*/;

	static native boolean isSupported () /*-{
		return "Accelerometer" in $wnd;
	}-*/;

	final native double x () /*-{
		return this.x ? this.x : 0;
	}-*/;

	final native double y () /*-{
		return this.y ? this.y : 0;
	}-*/;

	final native double z () /*-{
		return this.z ? this.z : 0;
	}-*/;
}
