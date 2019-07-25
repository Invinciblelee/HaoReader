package com.monke.monkeybook.presenter.contract;

import com.google.android.material.snackbar.Snackbar;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.ReplaceRuleBean;

import java.io.File;
import java.util.List;

public interface ReplaceRuleContract {
    interface Presenter extends IPresenter {

        void saveData(List<ReplaceRuleBean> replaceRuleBeans);

        void delData(ReplaceRuleBean replaceRuleBean);

        void delData(List<ReplaceRuleBean> replaceRuleBeans);

        void editData(ReplaceRuleBean replaceRuleBean);

        void importDataS(File file);

        void importDataS(String url);
    }

    interface View extends IView {

        void refresh(List<ReplaceRuleBean> replaceRuleBeans);

        Snackbar getSnackBar(String msg);

        void showLoading(String msg);

        void dismissLoading();

        void toast(String msg);
    }

}
