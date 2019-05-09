package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.MarkdownUtils;

public class LargeTextDialog extends AppCompatDialog {

    public static void show(FragmentManager fragmentManager, String text, boolean markdown) {
        LargeTextDialog dialog = new LargeTextDialog();
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putBoolean("markdown", markdown);
        dialog.setArguments(args);
        dialog.show(fragmentManager, "largeText");
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_large_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppCompatTextView tvContent = findViewById(R.id.tv_text);
        Bundle args = getArguments();
        assert args != null;
        final String text = args.getString("text");
        final boolean markdown = args.getBoolean("markdown");

        if (markdown) {
            MarkdownUtils.setText(tvContent, text);
        } else {
            tvContent.setText(text);
        }


    }
}
