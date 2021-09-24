# 仿调频广播刻度指示选择器

1、下载 **FMTuneWheel\app\libs\MFTuneWheel.aar** 文件  导入自己项目中 **libs** 文件中

2、修改build.gradle 文件  
```
repositories {
    flatDir {
        dirs 'libs'
    }
}


implementation(name: 'MFTuneWheel', ext: 'aar')
```
3、使用示例  
```
    <com.lzhs.mftunewheel.MFTuneWheel
        android:id="@+id/mMFTuneWheel"
        android:layout_width="match_parent"
        android:layout_height="250dp"
        android:layout_marginTop="55dp"
        app:font_size="16"
        app:item_count="30"
        app:max_item="950"
        app:min_item="875"
        app:scale_height="40.0"
        app:scale_normal_color="@color/normal_color"
        app:scale_normal_width="5"
        app:scale_height_diff="15"
        app:scale_max_height_diff="35"
        app:scale_height_width="7"
        app:scale_normal_five_color="@color/white"
        app:text_color="@color/white"
        app:text_diff="20"
        />


        val mTextView = findViewById<TextView>(R.id.mTextView)
        findViewById<MFTuneWheel>(R.id.mMFTuneWheel).setListener {
            mTextView.text = it
        }

```


## 支持属性：   

|属性 |类型|默认值|说明|  
|:--|:--|:--|:--| 
|min_item|integer|875| 刻度盘上的最小值
|max_item|integer|950|刻度盘上的最大值（最小值和最大值的差必须大于等 显示刻度的总数）
|item_count|integer|30|需要显示 的刻度的总数( 必须大于16 个 小于55个)
|scale_height|float|40|普通刻度线的高度
|scale_normal_color|color|LTGRAY|普通刻度线的颜色
|scale_normal_width|float|5|普通刻度线的宽度
|scale_height_diff|float|22|普通刻度线的高度  与  5 刻度的高度差
|scale_max_height_diff|float|30|中线刻度线的高度  与  普通刻度的高度差
|scale_height_width|float|7|普通刻度线的宽度
|scale_normal_five_color|color|WHITE|刻度位5的 刻度线颜色
|font_size|integer|16|绘制文字的大小 
|text_diff|float|20|绘制的文字与刻度之间的距离
|text_color|color|WHITE|绘制文字的颜色  
 
## 效果图
 


     
