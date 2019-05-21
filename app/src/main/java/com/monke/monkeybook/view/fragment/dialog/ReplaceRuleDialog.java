package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.utils.StringUtils;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ReplaceRuleDialog extends AppCompatDialog {
    private TextInputEditText tieReplaceSummary;
    private TextInputEditText tieReplaceRule;
    private TextInputEditText tieReplaceTo;
    private TextInputEditText tieUseTo;
    private AppCompatCheckBox cbUseRegex;

    private OnSaveReplaceRule saveReplaceRule;
    private ReplaceRuleBean replaceRuleBean;

    public static void show(FragmentManager fragmentManager, ReplaceRuleBean replaceRuleBean, OnSaveReplaceRule saveReplaceRule) {
        ReplaceRuleDialog dialog = new ReplaceRuleDialog();
        Bundle args = new Bundle();
        args.putParcelable("replaceRuleBean", replaceRuleBean);
        dialog.setArguments(args);
        dialog.saveReplaceRule = saveReplaceRule;
        dialog.show(fragmentManager, "replaceRule");
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_replace_rule, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextInputLayout tilReplaceSummary = findViewById(R.id.til_replace_summary);
        tilReplaceSummary.setHint(getResources().getString(R.string.replace_rule_summary));
        TextInputLayout tilReplaceRule = findViewById(R.id.til_replace_rule);
        tilReplaceRule.setHint(getResources().getString(R.string.replace_rule));
        TextInputLayout tilReplaceTo = findViewById(R.id.til_replace_to);
        tilReplaceTo.setHint(getResources().getString(R.string.replace_to));
        TextInputLayout tilUseTo = findViewById(R.id.til_use_to);
        tilUseTo.setHint(getResources().getString(R.string.use_to));
        tieReplaceRule = findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = findViewById(R.id.tie_replace_summary);
        tieReplaceTo = findViewById(R.id.tie_replace_to);
        tieUseTo = findViewById(R.id.tie_use_to);
        cbUseRegex = findViewById(R.id.cb_use_regex);

        Bundle args = getArguments();
        assert args != null;
        replaceRuleBean = args.getParcelable("replaceRuleBean");

        if (replaceRuleBean != null) {
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
            cbUseRegex.setChecked(replaceRuleBean.getIsRegex());
        } else {
            this.replaceRuleBean = new ReplaceRuleBean();
            this.replaceRuleBean.setEnable(true);
            this.replaceRuleBean.setIsRegex(true);
        }

        View tvOk = findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(v -> {
            replaceRuleBean.setReplaceSummary(StringUtils.valueOf(tieReplaceSummary.getText()));
            replaceRuleBean.setRegex(StringUtils.valueOf(tieReplaceRule.getText()));
            replaceRuleBean.setReplacement(StringUtils.valueOf(tieReplaceTo.getText()));
            replaceRuleBean.setUseTo(StringUtils.valueOf(tieUseTo.getText()));
            replaceRuleBean.setIsRegex(cbUseRegex.isChecked());
            if (saveReplaceRule != null) {
                saveReplaceRule.saveReplaceRule(replaceRuleBean);
            }
            dismissAllowingStateLoss();
        });
    }


    /**
     * 输入替换规则完成
     */
    public interface OnSaveReplaceRule {
        void saveReplaceRule(ReplaceRuleBean replaceRuleBean);
    }
}
