/*
 *  Author(s): Kosuke Miyoshi, Narrative Nights
 */
package com.yurisuzuki.ar;

import com.yurisuzuki.CameraActivity;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.camera.CameraRotationInfo;

import javax.microedition.khronos.opengles.GL10;

public class GuitarCodeMarker extends Marker {
	/// trackingが外れた後に、holdを継続する時間
	private static final long HOLDING_DURATION_MILLIS = 30 * 1000;

	private long holdStartTime = -1L;

	void checkHold(long now, CameraActivity activity) {
		if (isTracked()) {
			// マーカーを認識していたら、lastTrackedTimeを更新
			lastTrackedTime = now;
			holdStartTime = -1L;
			activity.suppressCurrentSound(soundId);
		} else {
			if (lastTrackedTime > 0 && holdStartTime < 0) {
				// hold開始
				lastTrackedTime = -1;
				holdStartTime = now;
				activity.setCurrentSound(soundId);
			} else if (lastTrackedTime == -1L && holdStartTime >= 0) {
				// hold中
				if (now - holdStartTime > HOLDING_DURATION_MILLIS) {
					// hold開始してから一定時間経ったのでholdを終了する
					activity.stopCurrentSound(soundId);
					lastTrackedTime = -1L;
					holdStartTime = -1L;
				}
			}
		}
	}

	private boolean exclusiveHold = false;

	void updateExclusiveHold(CameraActivity activity) {
		exclusiveHold = activity.checkMarkerHolded(soundId);
	}

	@Override
	boolean draw(GL10 gl, long now, CameraRotationInfo camreaCameraRotationInfo) {
		if (isTracked()) {
			float markerMatrix[] = ARToolKit.getInstance().queryMarkerTransformation(markerId);

			if( markerMatrix != null ) {
				adjustMarkerMatrix(markerMatrix, adjustedMarkerMatrix, camreaCameraRotationInfo);
				cacheMarkerMatrix(adjustedMarkerMatrix);
			}

			if (lastTrackedTime > 0) {
				if (markerMatrix != null) {
					gl.glLoadMatrixf(adjustedMarkerMatrix, 0);
					markerPlane.draw(gl);
				}
			}
		}

		if (exclusiveHold && markerMatrixCached) {
			gl.glLoadMatrixf(cachedMarkerMatrix, 0);
			actionPlane.draw(gl);
			return true;
		} else {
			return false;
		}
	}
}
