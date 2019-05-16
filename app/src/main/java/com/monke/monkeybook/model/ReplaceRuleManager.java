package com.monke.monkeybook.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.ReplaceRuleBeanDao;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManager extends BaseModelImpl {
    private List<ReplaceRuleBean> replaceRuleBeansEnabled;
    private List<ReplaceRuleBean> replaceRuleBeansAll;

    private ReplaceRuleManager() {

    }

    private volatile static ReplaceRuleManager mInstance;

    public static ReplaceRuleManager getInstance() {
        if (mInstance == null) {
            synchronized (ReplaceRuleManager.class) {
                if (mInstance == null) {
                    mInstance = new ReplaceRuleManager();
                }
            }
        }
        return mInstance;
    }

    public List<ReplaceRuleBean> getEnabled() {
        if (replaceRuleBeansEnabled == null) {
            replaceRuleBeansEnabled = DbHelper.getInstance().getDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansEnabled;
    }

    public List<ReplaceRuleBean> getAll() {
        if (replaceRuleBeansAll == null) {
            replaceRuleBeansAll = DbHelper.getInstance().getDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansAll;
    }

    public void saveData(ReplaceRuleBean replaceRuleBean) {
        if (replaceRuleBean.getSerialNumber() == 0) {
            replaceRuleBean.setSerialNumber(replaceRuleBeansAll.size() + 1);
        }
        DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
        refreshDataS();
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        refreshDataS();
    }

    public void saveDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
            refreshDataS();
        }
    }

    public void delDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
            DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        }
        refreshDataS();
    }

    private void refreshDataS() {
        replaceRuleBeansEnabled = DbHelper.getInstance().getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        replaceRuleBeansAll = DbHelper.getInstance().getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
    }

    public Observable<Boolean> importReplaceRuleFromWww(String url) {
        try {
            url = url.trim();
            if (URLUtils.isUrl(url)) {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(StringUtils.getBaseUrl(url), url);
                return SimpleModel.getResponse(analyzeUrl)
                        .flatMap(rsp -> importReplaceRuleO(rsp.body()))
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread());
            }
            throw new IllegalArgumentException("url is invalid");
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    private Observable<Boolean> importReplaceRuleO(String json) {
        return Observable.create(e -> {
            try {
                List<ReplaceRuleBean> replaceRuleBeans = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
                }.getType());
                saveDataS(replaceRuleBeans);
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }
}
