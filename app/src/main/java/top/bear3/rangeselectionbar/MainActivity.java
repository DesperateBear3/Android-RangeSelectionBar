package top.bear3.rangeselectionbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements RangeSelectionBar.OnRangeSelectionBarChangeListener{
    private static final String TAG = "MainActivity";

    private RangeSelectionBar selectionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectionBar = findViewById(R.id.sb);
        selectionBar.setOnRangeSelectionBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(RangeSelectionBar rangeSelectionBar, int lowProgress, int highProgress) {
        Log.d(TAG, "onProgressChanged: low -- " + lowProgress + "  high -- " + highProgress);
    }

    @Override
    public void onStartTrackingTouch(RangeSelectionBar rangeSelectionBar) {
        Log.d(TAG, "onStartTrackingTouch: low -- " + rangeSelectionBar.getLow() + "  high -- " + rangeSelectionBar.getHigh());
    }

    @Override
    public void onStopTrackingTouch(RangeSelectionBar rangeSelectionBar) {
        Log.d(TAG, "onStopTrackingTouch: low -- " + rangeSelectionBar.getLow() + "  high -- " + rangeSelectionBar.getHigh());
    }
}
