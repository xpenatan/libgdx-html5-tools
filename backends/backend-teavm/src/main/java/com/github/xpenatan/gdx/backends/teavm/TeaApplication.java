package com.github.xpenatan.gdx.backends.teavm;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.xpenatan.gdx.backends.teavm.agent.TeaAgentInfo;
import com.github.xpenatan.gdx.backends.teavm.agent.TeaWebAgent;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetInstance;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.backends.teavm.dom.EventListenerWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.LocationWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.impl.TeaWindow;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetDownloadImpl;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoadImpl;
import com.github.xpenatan.gdx.backends.teavm.utils.TeaNavigator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * @author xpenatan
 */
public class TeaApplication implements Application, Runnable {

    private static TeaAgentInfo agentInfo;

    public int delayInitCount;

    public static TeaAgentInfo getAgentInfo() {
        return agentInfo;
    }

    public static TeaApplication get() {
        return (TeaApplication)Gdx.app;
    }

    private TeaGraphics graphics;
    private TeaInput input;
    private TeaFiles files;
    private TeaNet net;
    private TeaAudio audio;
    private TeaApplicationConfiguration config;
    private ApplicationListener queueAppListener;
    private ApplicationListener appListener;
    private final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>(4);
    private TeaWindow window;

    private AppState initState = AppState.INIT;

    private int lastWidth = -1;
    private int lastHeight = 1;

    private ApplicationLogger logger;
    private int logLevel = LOG_ERROR;

    private AssetLoadImpl assetLoader;

    private ObjectMap<String, Preferences> prefs = new ObjectMap<>();

    private TeaClipboard clipboard;

    private Array<Runnable> runnables = new Array<Runnable>();
    private Array<Runnable> runnablesHelper = new Array<Runnable>();

    private String hostPageBaseURL;

    public TeaApplication(ApplicationListener appListener, TeaApplicationConfiguration config) {
        this.window = TeaWindow.get();
        this.config = config;
        setApplicationListener(appListener);
        init();
    }

    private void init() {
        TeaApplication.agentInfo = TeaWebAgent.computeAgentInfo();
        System.setProperty("java.runtime.name", "");
        System.setProperty("userAgent", TeaApplication.agentInfo.getUserAgent());
        if(agentInfo.isWindows())
            System.setProperty("os.name", "Windows");
        else if(agentInfo.isMacOS())
            System.setProperty("os.name", "OS X");
        else if(agentInfo.isLinux())
            System.setProperty("os.name", "Linux");
        else
            System.setProperty("os.name", "no OS");

        AssetDownloadImpl assetDownload = new AssetDownloadImpl(config.showDownloadLogs);
        AssetInstance.setInstance(assetDownload);

        TeaWindow currentWindow = TeaWindow.get();
        LocationWrapper location = currentWindow.getLocation();
        hostPageBaseURL = location.getHref();

        if(hostPageBaseURL.contains(".html")) {
            // TODO use regex
            hostPageBaseURL = hostPageBaseURL.replace("index.html", "");
            hostPageBaseURL = hostPageBaseURL.replace("index-debug.html", "");
        }
        int indexQM = hostPageBaseURL.indexOf('?');
        if (indexQM >= 0) {
          hostPageBaseURL = hostPageBaseURL.substring(0, indexQM);
        }

        graphics = new TeaGraphics(config);

        assetLoader = new AssetLoadImpl(hostPageBaseURL, graphics.canvas, this, assetDownload);
        AssetInstance.setInstance(assetLoader);

        if(config.useUpdatedInputClass) {
            input = new DefaultTeaInput(graphics.canvas, config);
        }
        else {
            input = new TeaInputOld(this, graphics.canvas);
        }
        files = new TeaFiles(config, this);
        net = new TeaNet();
        logger = new TeaApplicationLogger();
        clipboard = new TeaClipboard();

        initGdx();
        initSound();

        if(config.preloadListener != null) {
            config.preloadListener.onPreload(assetLoader);
        }

        Gdx.app = this;
        Gdx.graphics = graphics;
        Gdx.gl = graphics.getGL20();
        Gdx.gl20 = graphics.getGL20();
        Gdx.gl30 = graphics.getGL30();
        Gdx.input = input;
        Gdx.files = files;
        Gdx.net = net;

        audio = new DefaultTeaAudio();
        Gdx.audio = audio;

        window.addEventListener("pagehide", new EventListenerWrapper() {
            @Override
            public void handleEvent(EventWrapper evt) {
                if(appListener != null) {
                    appListener.pause();
                    appListener.dispose();
                    appListener = null;
                }
            }
        });

        window.getDocument().addEventListener("visibilitychange", new EventListenerWrapper() {
            @Override
            public void handleEvent(EventWrapper evt) {
                // notify of state change
                if(initState == AppState.APP_LOOP) {
                    String state = window.getDocument().getVisibilityState();
                    if (state.equals("hidden")) {
                        // hidden: i.e. we are paused
                        synchronized (lifecycleListeners) {
                            for (LifecycleListener listener : lifecycleListeners) {
                                listener.pause();
                            }
                        }
                        appListener.pause();
                    }
                    else if(state.equals("visible")){
                        // visible: i.e. we resume
                        synchronized (lifecycleListeners) {
                            for (LifecycleListener listener : lifecycleListeners) {
                                listener.resume();
                            }
                        }
                        appListener.resume();
                    }
                }
            }
        });

        if(config.isAutoSizeApplication()) {
            window.addEventListener("resize", new EventListenerWrapper() {
                @Override
                public void handleEvent(EventWrapper evt) {
                    int width = window.getClientWidth() - config.padHorizontal;
                    int height = window.getClientHeight() - config.padVertical;

                    if(width <= 0 || height <= 0) {
                        return;
                    }

                    if(graphics != null) {
                        graphics.setCanvasSize(width, height, config.usePhysicalPixels);
                    }
                }
            });
        }

        window.requestAnimationFrame(this);
    }

    @Override
    public void run() {
        AppState state = initState;
        try {
            switch(state) {
                case INIT:
                    if(delayInitCount == 0) {
                        initState = AppState.PRELOAD_ASSETS;
                    }
                    break;
                case PRELOAD_ASSETS:
                    assetLoader.preload(config, "assets.txt");
                    initState = AppState.DOWNLOAD_ASSETS;
                    break;
                case DOWNLOAD_ASSETS:
                    int queue = assetLoader.getQueue();
                    if(queue == 0) {
                        initState = AppState.APP_LOOP;

                        // remove loading indicator
                        HTMLElement element = Window.current().getDocument().getElementById("progress");
                        if (element != null) {
                          element.getStyle().setProperty("display", "none");
                        }
                    }
                    else {
                        // update progress bar once we know the total number of assets that are loaded
                        int total = assetLoader.assetTotal;
                        if (total > 0) {
                          // we have the actual total and can update the progress bar
                          int minPercentage = 25;
                          int percentage = minPercentage + (((100 - minPercentage) * (total - queue)) / total);
                          HTMLElement progressBar = Window.current().getDocument().getElementById("progress-bar");
                          if (progressBar != null) {
                            progressBar.getStyle().setProperty("width", percentage + "%");
                          }
                        }
                    }
                    break;
                case APP_LOOP:
                    if(queueAppListener != null) {
                        if(appListener != null) {
                            appListener.pause();
                            appListener.dispose();
                        }
                        input.setInputProcessor(null);
                        input.reset();
                        runnables.clear();
                        appListener = queueAppListener;
                        queueAppListener = null;
                        initState = AppState.APP_CREATE;
                        graphics.frameId  = 0;
                    }
                    if(appListener != null) {
                        step(appListener);
                    }
                    break;
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
            throw t;
        }

        window.requestAnimationFrame(this);
    }

    private void step(ApplicationListener appListener) {
        graphics.update();
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        boolean resizeBypass = false;

        if(initState == AppState.APP_CREATE) {
            initState = AppState.APP_LOOP;
            appListener.create();
            appListener.resume();
            resizeBypass = true;
        }

        if((width != lastWidth || height != lastHeight) || resizeBypass) {
            lastWidth = width;
            lastHeight = height;
            Gdx.gl.glViewport(0, 0, width, height);
            appListener.resize(width, height);
        }

        runnablesHelper.addAll(runnables);
        runnables.clear();
        for(int i = 0; i < runnablesHelper.size; i++) {
            runnablesHelper.get(i).run();
        }
        runnablesHelper.clear();
        graphics.frameId++;
        if(graphics.frameId > 60) { // A bit of delay before rendering so fps don't start with 0
            appListener.render();
        }
        input.reset();
    }

    public void setApplicationListener(ApplicationListener applicationListener) {
        this.queueAppListener = applicationListener;
    }

    public TeaApplicationConfiguration getConfig() {
        return config;
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return appListener;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void log(String tag, String message) {
        if(logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if(logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if(logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if(logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        if(logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if(logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.logger = applicationLogger;
    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return logger;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.WebGL;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public long getJavaHeap() {
        return 0;
    }

    @Override
    public long getNativeHeap() {
        return 0;
    }

    @Override
    public Preferences getPreferences(String name) {
        Preferences pref = prefs.get(name);
        if(pref == null) {
            Storage storage = Storage.getLocalStorage();;
            pref = new TeaPreferences(storage, config.storagePrefix + ":" + name, config.shouldEncodePreference);
            prefs.put(name, pref);
        }
        return pref;
    }

    @Override
    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        runnables.add(runnable);
    }

    @Override
    public void exit() {
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    /** @return {@code true} if application runs on a mobile device */
    public static boolean isMobileDevice () {
        // RegEx pattern from detectmobilebrowsers.com (public domain)
        String pattern = "(android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec"
                + "|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)"
                + "i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)"
                + "|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(TeaNavigator.getUserAgent().toLowerCase());
        return m.matches();
    }

    public enum AppState {
        INIT,
        PRELOAD_ASSETS,
        DOWNLOAD_ASSETS,
        APP_CREATE,
        APP_LOOP
    }

    // Testing code only
    @JSBody(params = "text", script = "console.log(text);" )
    public static native void print(String text);

    // ##################### NATIVE CALLS #####################

    private void initGdx() {
        assetLoader.loadScript("gdx.wasm.js", new AssetLoaderListener<>() {});
    }

    private void initSound() {
        assetLoader.loadScript("howler.js", new AssetLoaderListener<>() {});
    }
}