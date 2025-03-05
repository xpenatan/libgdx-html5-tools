package com.github.xpenatan.gdx.backends.teavm;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.xpenatan.gdx.backends.teavm.agent.TeaAgentInfo;
import com.github.xpenatan.gdx.backends.teavm.dom.ElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventListenerWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventTargetWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLCanvasElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLDocumentWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.KeyboardEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.MouseEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.TouchEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.TouchListWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.TouchWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.WheelEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.utils.KeyCodesOld;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;

/**
 * @author xpenatan
 */
public class TeaInputOld extends AbstractInput implements EventListenerWrapper, TeaInput {

    private static float getMouseWheelVelocity(WheelEventWrapper event) {
        TeaAgentInfo agent = TeaApplication.getAgentInfo();
        float delta = 0;
        float detail = event.getDetail();
        float wheelDelta = event.getWheelDelta();
        if(agent.isFirefox()) {
            if(agent.isMacOS()) {
                delta = 1.0f * detail;
            }
            else {
                delta = 1.0f * detail / 3f;
            }
        }
        else if(agent.isOpera()) {
            if(agent.isLinux()) {
                delta = -1.0f * wheelDelta / 80f;
            }
            else {
                // on mac
                delta = -1.0f * wheelDelta / 40f;
            }
        }
        else if(agent.isChrome() || agent.isSafari() || agent.isIE()) {
            delta = -1.0f * wheelDelta / 120f;
            // handle touchpad for chrome
            if(Math.abs(delta) < 1) {
                if(agent.isWindows()) {
                    delta = -1.0f * wheelDelta;
                }
                else if(agent.isMacOS()) {
                    delta = -1.0f * wheelDelta / 3;
                }
            }
        }
        return delta;
    }

    private HTMLCanvasElementWrapper canvas;

    static final int MAX_TOUCHES = 20;
    boolean justTouched = false;
    private IntMap<Integer> touchMap = new IntMap<>(20);
    private boolean[] touched = new boolean[MAX_TOUCHES];
    private int[] touchX = new int[MAX_TOUCHES];
    private int[] touchY = new int[MAX_TOUCHES];
    private int[] deltaX = new int[MAX_TOUCHES];
    private int[] deltaY = new int[MAX_TOUCHES];
    IntSet pressedButtons = new IntSet();
    int pressedKeyCount = 0;
    boolean keyJustPressed = false;
    boolean[] justPressedButtons = new boolean[5];
    InputProcessor processor;
    long currentEventTimeStamp;
    boolean hasFocus = true;

    private TeaApplication application;

    public TeaInputOld(TeaApplication application, HTMLCanvasElementWrapper canvas) {
        this.application = application;
        this.canvas = canvas;
        hookEvents();
    }

    private void hookEvents() {
        HTMLDocumentWrapper document = canvas.getOwnerDocument();
        document.addEventListener("mousedown", this, false);
        document.addEventListener("mouseup", this, false);
        document.addEventListener("mousemove", this, false);
        document.addEventListener("wheel", this, false);

        document.addEventListener("keydown", this, false);
        document.addEventListener("keyup", this, false);
        document.addEventListener("keypress", this, false);

        canvas.addEventListener("touchstart", this, true);
        canvas.addEventListener("touchmove", this, true);
        canvas.addEventListener("touchcancel", this, true);
        canvas.addEventListener("touchend", this, true);
    }

    @Override
    public void handleEvent(EventWrapper e) {
        if(application.getApplicationListener() != null) {
            handleMouseEvents(e);
            handleKeyboardEvents(e);
        }
    }

    private void handleMouseEvents(EventWrapper e) {
        String type = e.getType();
        if(type.equals("mousedown")) {
            // makes sure the game has keyboard focus (e.g. when embedded in iFrame on itch.io)
            Window.current().focus();

            MouseEventWrapper mouseEvent = (MouseEventWrapper)e;
            EventTargetWrapper target = e.getTarget();
            HTMLCanvasElementWrapper canvas2 = canvas;
            boolean equals = target == canvas2;
            if(!equals || touched[0]) { // TODO needs check equals bug
                float mouseX = getRelativeX(mouseEvent, canvas);
                float mouseY = getRelativeY(mouseEvent, canvas);
                if(mouseX < 0 || mouseX > Gdx.graphics.getWidth() || mouseY < 0 || mouseY > Gdx.graphics.getHeight()) {
                    hasFocus = false;
                }
                return;
            }
            hasFocus = true;
            this.justTouched = true;
            this.touched[0] = true;
            int button = KeyCodesOld.getButton(mouseEvent.getButton());
            this.pressedButtons.add(button);
            justPressedButtons[button] = true;
            this.deltaX[0] = 0;
            this.deltaY[0] = 0;
            if(isCursorCatched()) {
                this.touchX[0] += mouseEvent.getMovementX();
                this.touchY[0] += mouseEvent.getMovementY();
            }
            else {
                int relativeX = getRelativeX(mouseEvent, canvas);
                int relativeY = getRelativeY(mouseEvent, canvas);
                this.touchX[0] = relativeX;
                this.touchY[0] = relativeY;
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            if(processor != null)
                processor.touchDown(touchX[0], touchY[0], 0, KeyCodesOld.getButton(mouseEvent.getButton()));

            e.preventDefault();
            e.stopPropagation();
        }
        else if(type.equals("mouseup")) {
            MouseEventWrapper mouseEvent = (MouseEventWrapper)e;
            if(!touched[0]) return;
            this.pressedButtons.remove(KeyCodesOld.getButton(mouseEvent.getButton()));
            this.touched[0] = pressedButtons.size > 0;
            if(isCursorCatched()) {
                setDelta(0, (int)mouseEvent.getMovementX(), (int)mouseEvent.getMovementY());
                this.touchX[0] += mouseEvent.getMovementX();
                this.touchY[0] += mouseEvent.getMovementY();
            }
            else {
                setDelta(0, getRelativeX(mouseEvent, canvas) - touchX[0], getRelativeY(mouseEvent, canvas) - touchY[0]);
                this.touchX[0] = getRelativeX(mouseEvent, canvas);
                this.touchY[0] = getRelativeY(mouseEvent, canvas);
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            this.touched[0] = false;
            if(processor != null)
                processor.touchUp(touchX[0], touchY[0], 0, KeyCodesOld.getButton(mouseEvent.getButton()));
        }
        else if(type.equals("mousemove")) {
            MouseEventWrapper mouseEvent = (MouseEventWrapper)e;
            if(isCursorCatched()) {
                setDelta(0, (int)mouseEvent.getMovementX(), (int)mouseEvent.getMovementY());
                this.touchX[0] += mouseEvent.getMovementX();
                this.touchY[0] += mouseEvent.getMovementY();
            }
            else {
                int relativeX = getRelativeX(mouseEvent, canvas);
                int relativeY = getRelativeY(mouseEvent, canvas);
                setDelta(0, relativeX - touchX[0], relativeY - touchY[0]);
                this.touchX[0] = relativeX;
                this.touchY[0] = relativeY;
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            if(processor != null) {
                if(touched[0])
                    processor.touchDragged(touchX[0], touchY[0], 0);
                else
                    processor.mouseMoved(touchX[0], touchY[0]);
            }
        }
        else if(type.equals("wheel")) {
            WheelEventWrapper wheel = (WheelEventWrapper)e;
            if(processor != null) {
                float wheelDelta = getMouseWheelVelocity(wheel);
                processor.scrolled(0, (int)wheelDelta);
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
        }
        else if(type.equals("touchstart")) {
            this.justTouched = true;
            TouchEventWrapper touchEvent = (TouchEventWrapper)e;
            TouchListWrapper touches = touchEvent.getChangedTouches();
            for(int i = 0, j = touches.getLength(); i < j; i++) {
                TouchWrapper touch = touches.item(i);
                int real = touch.getIdentifier();
                int touchId;
                touchMap.put(real, touchId = getAvailablePointer());
                touched[touchId] = true;
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                deltaX[touchId] = 0;
                deltaY[touchId] = 0;
                if(processor != null) {
                    processor.touchDown(touchX[touchId], touchY[touchId], touchId, Buttons.LEFT);
                }
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            e.preventDefault();
        }
        if(type.equals("touchmove")) {
            TouchEventWrapper touchEvent = (TouchEventWrapper)e;
            TouchListWrapper touches = touchEvent.getChangedTouches();
            for(int i = 0, j = touches.getLength(); i < j; i++) {
                TouchWrapper touch = touches.item(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                setDelta(touchId, getRelativeX(touch, canvas) - touchX[touchId], getRelativeY(touch, canvas) - touchY[touchId]);
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                if(processor != null) {
                    processor.touchDragged(touchX[touchId], touchY[touchId], touchId);
                }
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            e.preventDefault();
        }
        if(type.equals("touchcancel")) {
            TouchEventWrapper touchEvent = (TouchEventWrapper)e;
            TouchListWrapper touches = touchEvent.getChangedTouches();
            for(int i = 0, j = touches.getLength(); i < j; i++) {
                TouchWrapper touch = touches.item(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                touchMap.remove(real);
                touched[touchId] = false;
                int relativeX = getRelativeX(touch, canvas);
                int relativeY = getRelativeY(touch, canvas);
                setDelta(touchId, relativeX - touchX[touchId], relativeY - touchY[touchId]);
                touchX[touchId] = relativeX;
                touchY[touchId] = relativeY;
                if(processor != null) {
                    processor.touchUp(touchX[touchId], touchY[touchId], touchId, Buttons.LEFT);
                }
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            e.preventDefault();
        }
        if(type.equals("touchend")) {
            TouchEventWrapper touchEvent = (TouchEventWrapper)e;
            TouchListWrapper touches = touchEvent.getChangedTouches();
            for(int i = 0, j = touches.getLength(); i < j; i++) {
                TouchWrapper touch = touches.item(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                touchMap.remove(real);
                touched[touchId] = false;
                int relativeX = getRelativeX(touch, canvas);
                int relativeY = getRelativeY(touch, canvas);
                setDelta(touchId, relativeX - touchX[touchId], relativeY - touchY[touchId]);
                touchX[touchId] = relativeX;
                touchY[touchId] = relativeY;
                if(processor != null) {
                    processor.touchUp(touchX[touchId], touchY[touchId], touchId, Buttons.LEFT);
                }
            }
            this.currentEventTimeStamp = TimeUtils.nanoTime();
            e.preventDefault();
        }
    }

    private void handleKeyboardEvents(EventWrapper e) {
        String type = e.getType();

        if(type.equals("keydown") && hasFocus) {
            KeyboardEventWrapper keyboardEvent = (KeyboardEventWrapper)e;
            int code = KeyCodesOld.keyForCode(keyboardEvent.getKeyCode());
            char keyChar = 0;

            switch(code) {
                case Keys.DEL:
                    keyChar = 8;
                    break;
                case Keys.FORWARD_DEL:
                    keyChar = 127;
                    break;
            }

            if(isCatchKey(code)) {
                e.preventDefault();
            }

            if(code == Input.Keys.DEL || code == Keys.FORWARD_DEL) {
                e.preventDefault();
                if(processor != null) {
                    processor.keyDown(code);
                    processor.keyTyped(keyChar);
                }
            }
            else {
                if(!pressedKeys[code]) {
                    pressedKeyCount++;
                    pressedKeys[code] = true;
                    keyJustPressed = true;
                    justPressedKeys[code] = true;
                    if(processor != null) {
                        processor.keyDown(code);
                    }
                }
            }

            // prevent TAB-key propagation, i.e. we handle ourselves!
            if(code == Keys.TAB) {
                e.preventDefault();
                e.stopPropagation();
            }
        }
        else if(type.equals("keypress") && hasFocus) {
            KeyboardEventWrapper keyboardEvent = (KeyboardEventWrapper)e;
            char c = (char)keyboardEvent.getCharCode();
            if(processor != null) processor.keyTyped(c);

            // prevent TAB-key propagation, i.e. we handle ourselves!
            if(c == '\t') {
                e.preventDefault();
                e.stopPropagation();
            }
        }

        else if(type.equals("keyup") && hasFocus) {
            KeyboardEventWrapper keyboardEvent = (KeyboardEventWrapper)e;
            int code = KeyCodesOld.keyForCode(keyboardEvent.getKeyCode());

            if(isCatchKey(code)) {
                e.preventDefault();
            }

            if(pressedKeys[code]) {
                pressedKeyCount--;
                pressedKeys[code] = false;
            }
            if(processor != null) {
                processor.keyUp(code);
            }

            // prevent TAB-key propagation, i.e. we handle ourselves!
            if(code == Keys.TAB) {
                e.preventDefault();
                e.stopPropagation();
            }
        }
    }

    private int getAvailablePointer() {
        for(int i = 0; i < MAX_TOUCHES; i++) {
            if(!touchMap.containsValue(i, false)) return i;
        }
        return -1;
    }

    public void reset() {
        if(justTouched) {
            justTouched = false;
            for(int i = 0; i < justPressedButtons.length; i++) {
                justPressedButtons[i] = false;
            }
        }

        if(keyJustPressed) {
            keyJustPressed = false;
            for(int i = 0; i < justPressedKeys.length; i++) {
                justPressedKeys[i] = false;
            }
        }
        for(int i = 0; i < touchX.length; i++) {
            deltaX[i] = 0;
            deltaY[i] = 0;
        }
    }

    public void setDelta(int touchId, int x, int y) {
        deltaX[touchId] = x;
        deltaY[touchId] = y;
    }

    private ElementWrapper getCompatMode(HTMLDocumentWrapper target) {
        String compatMode = target.getCompatMode();
        boolean isComp = compatMode.equals("CSS1Compat");
        ElementWrapper element = isComp ? target.getDocumentElement() : target;
        return element;
    }

    private int getScrollTop(ElementWrapper target) {
        int val = target.getScrollTop();
        return toInt32(val);
    }

    private int getScrollTop(HTMLDocumentWrapper target) {
        ElementWrapper element = getCompatMode(target);
        return getScrollTop(element);
    }

    private int getScrollLeft(ElementWrapper target) {
        int val = target.getScrollLeft();
        return toInt32(val);
    }

    private int getScrollLeft(HTMLDocumentWrapper target) {
        ElementWrapper element = getCompatMode(target);
        return getScrollLeft(element);
    }

    private int getRelativeX(HTMLCanvasElementWrapper target, TouchWrapper touch) {
        return touch.getClientX() - getAbsoluteLeft(target) + getScrollLeft(target)
                + getScrollLeft(target.getOwnerDocument());
    }

    private int getRelativeY(HTMLCanvasElementWrapper target, TouchWrapper touch) {
        return touch.getClientY() - getAbsoluteTop(target) + getScrollTop(target)
                + getScrollTop(target.getOwnerDocument());
    }

    protected int getRelativeX(MouseEventWrapper e, HTMLCanvasElementWrapper target) {
        float xScaleRatio = target.getWidth() * 1f / getClientWidth(target); // Correct for canvas CSS scaling
        return Math.round(xScaleRatio
                * (e.getClientX() - getAbsoluteLeft(target) + getScrollLeft(target) + getScrollLeft(target.getOwnerDocument())));
    }

    protected int getRelativeY(MouseEventWrapper e, HTMLCanvasElementWrapper target) {
        float yScaleRatio = target.getHeight() * 1f / getClientHeight(target); // Correct for canvas CSS scaling
        return Math.round(yScaleRatio
                * (e.getClientY() - getAbsoluteTop(target) + getScrollTop(target) + getScrollTop(target.getOwnerDocument())));
    }

    protected int getRelativeX(TouchWrapper touch, HTMLCanvasElementWrapper target) {
        float xScaleRatio = target.getWidth() * 1f / getClientWidth(target); // Correct for canvas CSS scaling
        return Math.round(xScaleRatio * getRelativeX(target, touch));
    }

    protected int getRelativeY(TouchWrapper touch, HTMLCanvasElementWrapper target) {
        float yScaleRatio = target.getHeight() * 1f / getClientHeight(target); // Correct for canvas CSS scaling
        return Math.round(yScaleRatio * getRelativeY(target, touch));
    }

    private int  getClientWidth(HTMLCanvasElementWrapper target) {
        return target.getClientWidth();
    }

    private int getClientHeight(HTMLCanvasElementWrapper target) {
        return target.getClientHeight();
    }

    private int getAbsoluteTop(HTMLCanvasElementWrapper target) {
        return toInt32(getSubPixelAbsoluteTop(target));
    }

    private double getSubPixelAbsoluteTop(HTMLElementWrapper elem) {
        float top = 0;
        HTMLElementWrapper curr = elem;
        while(curr.getOffsetParent() != null) {
            top -= curr.getScrollTop();
            curr = (HTMLElementWrapper)curr.getParentNode();
        }

        while(elem != null) {
            top += elem.getOffsetTop();
            elem = curr.getOffsetParent();
        }

        return top;
    }

    private int getAbsoluteLeft(HTMLCanvasElementWrapper target) {
        return toInt32(getSubPixelAbsoluteLeft(target));
    }

    private double getSubPixelAbsoluteLeft(HTMLElementWrapper elem) {
        float left = 0;
        HTMLElementWrapper curr = elem;
        while(curr.getOffsetParent() != null) {
            left -= curr.getScrollLeft();
            curr = (HTMLElementWrapper)curr.getParentNode();
        }

        while(elem != null) {
            left += elem.getOffsetLeft();
            elem = curr.getOffsetParent();
        }
        return left;
    }

    private static int toInt32(double val) {
        return (int)val;
    }

    @Override
    public float getAccelerometerX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getAccelerometerY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getAccelerometerZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getGyroscopeX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getGyroscopeY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getGyroscopeZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxPointers() {
        return MAX_TOUCHES;
    }

    @Override
    public int getX() {
        return touchX[0];
    }

    @Override
    public int getX(int pointer) {
        return touchX[pointer];
    }

    @Override
    public int getDeltaX() {
        return deltaX[0];
    }

    @Override
    public int getDeltaX(int pointer) {
        return deltaX[pointer];
    }

    @Override
    public int getY() {
        return touchY[0];
    }

    @Override
    public int getY(int pointer) {
        return touchY[pointer];
    }

    @Override
    public int getDeltaY() {
        return deltaY[0];
    }

    @Override
    public int getDeltaY(int pointer) {
        return deltaY[pointer];
    }

    @Override
    public boolean isTouched() {
        for(int pointer = 0; pointer < MAX_TOUCHES; pointer++) {
            if(touched[pointer]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean justTouched() {
        return justTouched;
    }

    @Override
    public boolean isTouched(int pointer) {
        return touched[pointer];
    }

    @Override
    public float getPressure() {
        return getPressure(0);
    }

    @Override
    public float getPressure(int pointer) {
        return isTouched(pointer) ? 1 : 0;
    }

    @Override
    public boolean isButtonPressed(int button) {
        return pressedButtons.contains(button) && touched[0];
    }

    @Override
    public boolean isButtonJustPressed(int button) {
        if(button < 0 || button >= justPressedButtons.length) return false;
        return justPressedButtons[button];
    }

    @Override
    public void getTextInput(TextInputListener listener, String title, String text, String hint) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOnscreenKeyboardVisible(boolean visible) {
        // TODO Auto-generated method stub

    }

    @Override
    public void vibrate(int milliseconds) {
        // TODO Auto-generated method stub

    }

    @Override
    public void vibrate(int milliseconds, boolean fallback) {
        // TODO Auto-generated method stub
    }

    @Override
    public void vibrate(int milliseconds, int amplitude, boolean fallback) {
        // TODO Auto-generated method stub
    }

    @Override
    public void vibrate(VibrationType vibrationType) {
        // TODO Auto-generated method stub
    }

    @Override
    public float getAzimuth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getPitch() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getRoll() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void getRotationMatrix(float[] matrix) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getCurrentEventTime() {
        return currentEventTimeStamp;
    }

    @Override
    public void setInputProcessor(InputProcessor processor) {
        this.processor = processor;
    }

    @Override
    public InputProcessor getInputProcessor() {
        return processor;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral) {
        if(peripheral == Peripheral.Accelerometer)
            return false;
        else if(peripheral == Peripheral.Compass)
            return false;
        else if(peripheral == Peripheral.HardwareKeyboard)
            return true;
//		else if (peripheral == Peripheral.MultitouchScreen)
//			return isTouchScreen();
        else if(peripheral == Peripheral.OnscreenKeyboard)
            return false;
        else if(peripheral == Peripheral.Vibrator)
            return false;
        return false;
    }

    @Override
    public int getRotation() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Orientation getNativeOrientation() {
        return Orientation.Landscape;
    }

    @Override
    public void setCursorCatched(boolean catched) {
        if(catched) {
            setCursorCatchedJSNI(canvas);
        }
        else {
            exitCursorCatchedJSNI();
        }
    }

    @Override
    public boolean isCursorCatched() {
        return isCursorCatchedJSNI(canvas);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        // not possible in a browser window
    }

    @Override
    public void getTextInput(TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void openTextInputField(NativeInputConfiguration nativeInputConfiguration) {

    }

    @Override
    public void closeTextInputField(boolean b) {

    }

    @Override
    public void setKeyboardHeightObserver(KeyboardHeightObserver keyboardHeightObserver) {

    }

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     *
     * @param element Canvas
     */
    @JSBody(params = "element", script =
            "if (!element.requestPointerLock) {\n" +
            "   element.requestPointerLock = (function() {\n" +
            "       return element.webkitRequestPointerLock || element.mozRequestPointerLock;" +
            "   })();\n" +
            "}\n" +
            "element.requestPointerLock();"
    )
    private static native void setCursorCatchedJSNI(HTMLElementWrapper element);

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     */
    @JSBody(script =
            "document.exitPointerLock();"
    )
    private static native void exitCursorCatchedJSNI();

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     *
     * @return is Cursor catched
     */
    @JSBody(params = "element", script =
            "if (document.pointerLockElement === canvas || document.mozPointerLockElement === canvas) {\n" +
            "   return true;\n" +
            "}\n" +
            "return false;"
    )
    private static native boolean isCursorCatchedJSNI(HTMLElementWrapper canvas);
}
