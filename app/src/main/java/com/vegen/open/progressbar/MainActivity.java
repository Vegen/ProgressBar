package com.vegen.open.progressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vegen.open.library.ProgressBar;
import com.vegen.open.library.ProgressBarCoverAdapter;
import com.vegen.open.library.ProgressBarSpliceAdapter;
import com.vegen.open.library.TextWithStyle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar spliceProgressBar;
    private ProgressBar coverProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spliceProgressBar = findViewById(R.id.spliceProgressBar);
        coverProgressBar = findViewById(R.id.coverProgressBar);

        initSpliceProgressBar();
        initCoverProgressBar();
    }

    private void initSpliceProgressBar(){
        final List<TextWithStyle> spliceTextWithStyleList = new ArrayList<>();
        TextWithStyle text1 = new TextWithStyle("测试1：￥166", 14f, 0xFF2B78F6, 166f / (166 + 66 + 36));
        spliceTextWithStyleList.add(text1);
        TextWithStyle text2 = new TextWithStyle("测试2：￥66", 14f, 0xFFF5C900, 66f / (166 + 66 + 36));
        spliceTextWithStyleList.add(text2);
        TextWithStyle text3 = new TextWithStyle("测试3：￥36", 14f, new int[]{0xFF4FACFE, 0xFF00F2FE}, 36f / (166 + 66 + 36));
        spliceTextWithStyleList.add(text3);

        spliceProgressBar.setSpliceAdapter(new ProgressBarSpliceAdapter() {
            @Override
            public List<TextWithStyle> getTextList() {
                return spliceTextWithStyleList;
            }

            @Override
            public float getLineSpace() {
                return 10;
            }

            @Override
            public boolean isShowDotFront() {
                return true;
            }

            @Override
            public int getTextGravity() {
                return ProgressBarCoverAdapter.TEXT_GRAVITY_LEFT;
            }

            @Override
            public int getStartAngle() {
                return 90;
            }
        });
    }

    private void initCoverProgressBar(){

        final List<TextWithStyle> coverTextWithStyleList = new ArrayList<>();

        TextWithStyle text1 = new TextWithStyle("已用：￥80.8", 16f, new int[]{0xFF4FACFE, 0xFF00F2FE});
        coverTextWithStyleList.add(text1);
        TextWithStyle text2 = new TextWithStyle("未用：￥20", 14f, 0xFFBBC2CC);
        coverTextWithStyleList.add(text2);

        coverProgressBar.setCoverAdapter(new ProgressBarCoverAdapter() {
            @Override
            public List<TextWithStyle> getTextList() {
                return coverTextWithStyleList;
            }

            @Override
            public float getLineSpace() {
                return 10;
            }

            @Override
            public boolean isShowDotFront() {
                return true;
            }

            @Override
            public int getStartAngle() {
                return (int) (180 + (1 - getProgressPercent()) / 2f * 360);
            }

            @Override
            public float getProgressPercent() {
                return 80f / (80 + 20);
            }

            @Override
            public int getTextGravity() {
                return ProgressBarCoverAdapter.TEXT_GRAVITY_CENTER;
            }
        });
    }
}
