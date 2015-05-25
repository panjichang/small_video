package com.pan.simplepicture.activity;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.pan.simplepicture.R;
import com.pan.simplepicture.been.Material;
import com.pan.simplepicture.utils.MapUtil;
import com.pan.simplepicture.widget.MeterailEditText;
/**
 * 提供素材
 * @author pan
 *
 */
public class ProvideMaterialActivity extends BaseActivity {
	private final String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

	@Override
	protected void setActionBarTitle() {
		mActionBar.setTitle(title);
	}

	private String title;
	private MeterailEditText et_email;
	private MeterailEditText et_des;
	private MeterailEditText et_title;

	@Override
	protected void init() {
		title = getIntent().getStringExtra("title");
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_material);
		et_email = (MeterailEditText) findViewById(R.id.et_email);
		et_des = (MeterailEditText) findViewById(R.id.et_des);
		et_title = (MeterailEditText) findViewById(R.id.et_title);
		final Button btn_commit = (Button) findViewById(R.id.btn_commit);
		btn_commit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Material material = new Material();
				String title = et_title.getText().toString().trim();
				String des = et_des.getText().toString().trim();
				String email = et_email.getText().toString().trim();
				if (!TextUtils.isEmpty(email)) {
					if (!email.matches(check)) {
						Toast.makeText(ProvideMaterialActivity.this,
								"请输入正确的邮箱", 0).show();
						et_email.getFocus();
					}
					material.email = email;
					return;
				}
				if (TextUtils.isEmpty(title)) {
					Toast.makeText(ProvideMaterialActivity.this, "请输入标题", 0)
							.show();
					et_title.getFocus();
					return;
				}
				material.title = title;
				if (TextUtils.isEmpty(des)) {
					Toast.makeText(ProvideMaterialActivity.this, "请输入描述内容", 0)
							.show();
					et_des.getFocus();
					return;
				}
				material.des = des;
				MapUtil.saveProvideMaterial(material, new SaveCallback() {

					@Override
					public void done(AVException arg0) {
						showToast(true, "提交成功");
						et_email.setText("");
						et_title.setText("");
						et_des.setText("");
					}
				});
			}
		});
	}
}
