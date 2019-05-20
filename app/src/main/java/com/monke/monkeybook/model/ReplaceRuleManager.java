package com.monke.monkeybook.model;

import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.ReplaceRuleBeanDao;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManager extends BaseModelImpl {

    private static List<ReplaceRuleBean> sReplaceRuleBeansEnabled;

    public static List<ReplaceRuleBean> getEnabled() {
        if (sReplaceRuleBeansEnabled == null) {
            sReplaceRuleBeansEnabled = DbHelper.getInstance().getDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return sReplaceRuleBeansEnabled;
    }

    public static long getEnabledCount() {
        return getEnabled().size();
    }

    public static List<ReplaceRuleBean> getAll() {
        sReplaceRuleBeansEnabled = DbHelper.getInstance().getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();

        return DbHelper.getInstance().getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
    }

    public static void save(ReplaceRuleBean replaceRuleBean) {
        if (replaceRuleBean.getSerialNumber() == 0) {
            long count = DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().count();
            replaceRuleBean.setSerialNumber((int) count + 1);
        }
        DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
    }

    public static void delete(ReplaceRuleBean replaceRuleBean) {
        if (replaceRuleBean == null) return;
        DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
    }

    public static void saveAll(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
        }
    }

    public static void deleteAll(List<ReplaceRuleBean> replaceRuleBeans) {
        for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
            DbHelper.getInstance().getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        }
    }

    public static Observable<Boolean> importFromNet(String url) {
        try {
            url = url.trim();

            if (StringUtils.isJsonType(url)) {
                return importFromJson(url.trim());
            }

            if (URLUtils.isUrl(url)) {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(StringUtils.getBaseUrl(url), url);
                return SimpleModel.getResponse(analyzeUrl)
                        .subscribeOn(Schedulers.single())
                        .flatMap(rsp -> importFromJson(rsp.body()))
                        .observeOn(AndroidSchedulers.mainThread());
            }
            throw new IllegalArgumentException("url is invalid");
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    private static Observable<Boolean> importFromJson(String json) {
        return Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            List<ReplaceRuleBean> replaceRuleBeans = Global.GSON.fromJson(json.trim(), new TypeToken<List<ReplaceRuleBean>>() {
            }.getType());
            saveAll(replaceRuleBeans);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
