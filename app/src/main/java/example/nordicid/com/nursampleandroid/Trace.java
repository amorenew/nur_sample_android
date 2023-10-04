package example.nordicid.com.nursampleandroid;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class Trace extends Activity implements NurListener {
    //UI
    private Button mRefreshButton;
    private TextView mTraceableEpcEditText;
    private EditText mPctText;
    private ProgressBar mProgressBar;


    //ListView and adapter for showing tags found
    private ListView mTagsListView;
    private SimpleAdapter mTagsListViewAdapter;
    private ArrayList<HashMap<String, String>> mListViewAdapterData = new ArrayList<HashMap<String, String>>();

    //ProgressBar animation
    ObjectAnimator animation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace);
        NurHelper.getInstance().initReading();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPctText = (EditText) findViewById(R.id.pct_text);
        mTraceableEpcEditText = (TextView) findViewById(R.id.locate_epc_edittext);

        // Do not save EditText state
        mTraceableEpcEditText.setSaveEnabled(false);

        ViewGroup.LayoutParams lp = mProgressBar.getLayoutParams();
        WindowManager wm = (WindowManager) Trace.this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        lp.width = size.x / 3;
        lp.height = size.x / 3;
        mProgressBar.setLayoutParams(lp);
        mPctText.setLayoutParams(lp);

        mRefreshButton = (Button) findViewById(R.id.locate_button);
        mTagsListView = (ListView) findViewById(R.id.tags_listview);

        //sets the adapter for listview
        mTagsListViewAdapter = new SimpleAdapter(this, mListViewAdapterData, R.layout.taglist_row, new String[]{"epc", "rssi"}, new int[]{R.id.tagText, R.id.rssiText});
        mTagsListView.setAdapter(mTagsListViewAdapter);
        mTagsListView.setCacheColorHint(0);

        //List item selected
        mTagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked") final HashMap<String, String> mSelectedTag = (HashMap<String, String>) mTagsListView.getItemAtPosition(position);

                try {
                    NurHelper.getInstance().stopTrace();
                } catch (Exception ex) {
                    Toast.makeText(Trace.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                //Start tag tracing using selected tag
                try {
                    final String selectedEpc = mSelectedTag.get("epc");

                    NurHelper.getInstance().setTagTrace(selectedEpc);
                    mTraceableEpcEditText.setText("Trace tag: " + selectedEpc);

                    //..and start tracing..
                    final String result = NurHelper.getInstance().startTrace();
                    if (result.isEmpty()) {
                        mRefreshButton.setText("STOP");
                    } else {
                        Toast.makeText(Trace.this, result, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception ex) {
                    Toast.makeText(Trace.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //Refresh list Button OnClick handler
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Refresh tag list or stop tracing
                try {
                    if (NurHelper.getInstance().isTracingTag()) {
                        //Need to stop tag tracing
                        NurHelper.getInstance().stopTrace();
                        ShowProgressBar(0);
                        mTraceableEpcEditText.setText("");
                        return;
                    }

                    NurHelper.getInstance().clearInventoryReadings(); //Clear all from old stuff
                    NurHelper.getInstance().doSingleInventory(); //Make single round inventory.
                } catch (Exception ex) {
                    //Something fails..
                    Toast.makeText(Trace.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Update Progress bar. Show nice animation..
     */
    private void ShowProgressBar(int scaledRssi) {
        mProgressBar.setProgress(scaledRssi);
        mPctText.setText(scaledRssi + "%");

        if (animation != null) {
            NurHelper.getInstance().setLastRSSIValue((int) animation.getAnimatedValue());
            animation.cancel();
        } else {
            animation = ObjectAnimator.ofInt(mProgressBar, "progress", NurHelper.getInstance().getLastRSSIValue(), scaledRssi);
            animation.setInterpolator(new LinearInterpolator());
        }

        animation.setIntValues(NurHelper.getInstance().getLastRSSIValue(), scaledRssi);
        animation.setDuration(300);
        animation.start();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        NurHelper.getInstance().stopTrace();
    }

    @Override
    public void onPause() {
        super.onPause();
        NurHelper.getInstance().stopTrace();

    }


    @Override
    public void onStopTrace() {
        mRefreshButton.setText("Refresh");
    }

    @Override
    public void onTraceTagEvent(int scaledRssi) {
        ShowProgressBar(scaledRssi);
    }

    @Override
    public void onClearInventoryReadings() {
        mListViewAdapterData.clear();
        mTagsListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInventoryResult(HashMap<String, String> tmp) {
        mListViewAdapterData.add(tmp);
        mTagsListViewAdapter.notifyDataSetChanged();

    }
}
