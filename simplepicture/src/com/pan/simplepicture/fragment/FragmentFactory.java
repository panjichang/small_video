package com.pan.simplepicture.fragment;

import com.pan.simplepicture.activity.BaseActivity;

public class FragmentFactory {
	public static final int ALL = 0;
	public static final int CLASSIFY = 1;

	public static BaseFragment createFragment(int subId, BaseActivity activity) {
		BaseFragment fragment = null;
		switch (subId) {
		case ALL:
			fragment = new AllFragment(activity);
			break;
		case CLASSIFY:
			fragment = new ClassifyFragment(activity);
			break;
		default:
			break;
		}
		return fragment;
	}

}
