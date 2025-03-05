package com.github.xpenatan.gdx.backends.teavm;

/**
 * Port from GWT gdx
 *
 * @author xpenatan
 */
class TeaFeaturePolicy {

    static native boolean isSupported() /*-{
		return "featurePolicy" in $wnd.document;
	}-*/;

    static native boolean allowsFeature(String feature) /*-{
		if (!@com.badlogic.gdx.backends.gwt.GwtFeaturePolicy::isSupported()()) return true;
		return $wnd.document.featurePolicy.allowsFeature(feature);
	}-*/;

    static native boolean allowsFeature(String feature, String origin) /*-{
		if (!@com.badlogic.gdx.backends.gwt.GwtFeaturePolicy::isSupported()()) return true;
		return $wnd.document.featurePolicy.allowsFeature(feature, origin);
	}-*/;

    static private native JsArrayString JSfeatures() /*-{
		return $wnd.document.featurePolicy.features();
	}-*/;

    static String[] features() {
        if(TeaFeaturePolicy.isSupported())
            return toStringArray(JSfeatures());
        else
            return null;
    }

    static private native JsArrayString JSallowedFeatures() /*-{
		return $wnd.document.featurePolicy.allowedFeatures();
	}-*/;

    static String[] allowedFeatures() {
        if(TeaFeaturePolicy.isSupported())
            return toStringArray(JSallowedFeatures());
        else
            return null;
    }

    static private native JsArrayString JSgetAllowlistForFeature(String feature) /*-{
		return $wnd.document.featurePolicy.getAllowlistForFeature(feature);
	}-*/;

    static String[] getAllowlistForFeature(String feature) {
        if(TeaFeaturePolicy.isSupported())
            return toStringArray(JSgetAllowlistForFeature(feature));
        else
            return null;
    }
}
