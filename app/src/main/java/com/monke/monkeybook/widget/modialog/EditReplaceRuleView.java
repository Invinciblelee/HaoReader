package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.utils.KeyboardUtil;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditReplaceRuleView {
    private TextInputEditText tieReplaceSummary;
    private TextInputEditText tieReplaceRule;
    private TextInputEditText tieReplaceTo;
    private TextInputEditText tieUseTo;

    private MoDialogView moDialogView;
    private OnSaveReplaceRule saveReplaceRule;
    private Context context;
    private ReplaceRuleBean replaceRuleBean;

    public static EditReplaceRuleView newInstance(MoDialogView moDialogView) {
        return new EditReplaceRuleView(moDialogView);
    }

    private EditReplaceRuleView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
    }

    void showEditReplaceRule(ReplaceRuleBean replaceRuleBean, final OnSaveReplaceRule saveReplaceRule) {
        this.saveReplaceRule = saveReplaceRule;

        if (replaceRuleBean != null) {
            this.replaceRuleBean = replaceRuleBean;
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
        } else {
            this.replaceRuleBean = new ReplaceRuleBean();
            this.replaceRuleBean.setEnable(true);
        }
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_replace_rule, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceSummary = moDialogView.findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(context.getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = moDialogView.findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(context.getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = moDialogView.findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(context.getString(R.string.replace_to));
        TextInputLayout tilUseTo = moDialogView.findViewById(R.id.til_use_to);
        tilUseTo.setHint(context.getString(R.string.use_to));
        tieReplaceRule = moDialogView.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = moDialogView.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = moDialogView.findViewById(R.id.tie_replace_to);
        tieUseTo = moDialogView.findViewById(R.id.tie_use_to);

        View tvOk = moDialogView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            replaceRuleBean.setReplaceSummary(tieReplaceSummary.getText().toString());
            replaceRuleBean.setRegex(tieReplaceRule.getText().toString());
            replaceRuleBean.setReplacement(tieReplaceTo.getText().toString());
            replaceRuleBean.setUseTo(tieUseTo.getText().toString());
            saveReplaceRule.saveReplaceRule(replaceRuleBean);
            moDialogView.getMoDialogHUD().dismiss();
        });
        KeyboardUtil.resetViewPosition((Activity) context, moDialogView.findViewById(R.id.cv_root));
    }

    /**
     * 输入替换规则完成
     */
    public interface OnSaveReplaceRule {
        void saveReplaceRule(ReplaceRuleBean replaceRuleBean);
    }
}
