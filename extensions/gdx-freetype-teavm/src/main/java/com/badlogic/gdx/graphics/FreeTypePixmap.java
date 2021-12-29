package com.badlogic.gdx.graphics;

import com.github.xpenatan.gdx.backend.web.dom.CanvasRenderingContext2DWrapper;
import com.github.xpenatan.gdx.backend.web.dom.ImageDataWrapper;
import com.github.xpenatan.gdx.backend.web.dom.typedarray.ArrayBufferViewWrapper;
import com.github.xpenatan.gdx.backend.web.dom.typedarray.ArrayBufferWrapper;
import com.github.xpenatan.gdx.backend.web.dom.typedarray.Int8ArrayWrapper;
import com.github.xpenatan.gdx.backend.web.dom.typedarray.Uint8ClampedArrayWrapper;
import org.teavm.jso.JSBody;

import java.nio.ByteBuffer;


/** @author Simon Gerst */
public class FreeTypePixmap extends Pixmap {

	ByteBuffer buffer;

	public FreeTypePixmap (int width, int height, Format format) {
		super(width, height, format);
	}

	public void setPixelsNull () {
		pixels = null;
	}

	public ByteBuffer getRealPixels () {
		if (getWidth() == 0 || getHeight() == 0) {
			return FreeTypeUtil.newDirectReadWriteByteBuffer();
		}
		if (pixels == null) {
			CanvasRenderingContext2DWrapper context = getContext();
			ImageDataWrapper imageData = context.getImageData(0, 0, getWidth(), getHeight());
			pixels = imageData.getData();
			ArrayBufferWrapper buff = pixels.getBuffer();
			this.buffer = FreeTypeUtil.newDirectReadWriteByteBuffer(pixels);
			return this.buffer;
		}
		return buffer;
	}

	public void putPixelsBack (ByteBuffer pixels) {
		if (getWidth() == 0 || getHeight() == 0) return;
		ArrayBufferViewWrapper typedArray = FreeTypeUtil.getTypedArray(pixels);
		putPixelsBack(typedArray, getWidth(), getHeight(), getContext());
	}

	@JSBody(params = { "pixels", "width", "height", "ctx" }, script = "" +
			"var imgData = ctx.createImageData(width, height);" +
			"var data = imgData.data;" +
			"for (var i = 0, len = width * height * 4; i < len; i++) {" +
				"data[i] = pixels[i] & 0xff;" +
			"}" +
			"ctx.putImageData(imgData, 0, 0);")
	private static native void  putPixelsBack (ArrayBufferViewWrapper pixels, int width, int height, CanvasRenderingContext2DWrapper ctx);
}