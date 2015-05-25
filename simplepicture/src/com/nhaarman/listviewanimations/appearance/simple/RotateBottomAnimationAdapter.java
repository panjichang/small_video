/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.appearance.simple;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.pan.simplepicture.annotations.NonNull;

public class RotateBottomAnimationAdapter extends AnimationAdapter {

	private static final String ROTATION_X = "rotationX";
	private static final String TRANSLATION_Y = "translationY";
	private static final String ALPHA = "alpha";

	public RotateBottomAnimationAdapter(@NonNull final BaseAdapter baseAdapter) {
		super(baseAdapter);
	}

	@NonNull
	@Override
	public Animator[] getAnimators(@NonNull final ViewGroup parent,
			@NonNull final View view) {
		ObjectAnimator rotation = ObjectAnimator.ofFloat(view, ROTATION_X, 45,
				0);
		ObjectAnimator translation = ObjectAnimator.ofFloat(view,
				TRANSLATION_Y, 300, 0);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

		return new ObjectAnimator[] { rotation, translation, alpha };
	}

}
