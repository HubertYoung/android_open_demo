package com.het.sdk.demo.ui.fragment.device;

import android.os.Bundle;
import android.view.View;

import com.het.sdk.demo.R;
import com.het.sdk.demo.base.BaseHetActivity;
import com.het.sdk.demo.base.BaseHetFragment;
import com.het.sdk.demo.ui.activity.auth.ThirdAuthActivity;

import butterknife.OnClick;

/**
 * Created by Administrator on 2017-09-06.
 */

public class NoLoginFragment extends BaseHetFragment<LoginPresenter> {

    public static NoLoginFragment newInstance() {
        NoLoginFragment f = new NoLoginFragment();
        return f;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_no_login;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @OnClick({R.id.login1,R.id.login2,R.id.login3,R.id.login_cloud})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login1:
                mPresenter.startLogin(1);
                break;
            case R.id.login2:
                mPresenter.startLogin(2);
                break;
            case R.id.login3:
                mPresenter.startLogin(3);
                break;
            case R.id.login_cloud:
                ((BaseHetActivity) getActivity()).jumpToTarget(ThirdAuthActivity.class);
                break;
        }
    }

}
