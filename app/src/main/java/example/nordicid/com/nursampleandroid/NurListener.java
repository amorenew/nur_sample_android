package example.nordicid.com.nursampleandroid;

import java.util.HashMap;

interface NurListener {
    void onConnected(boolean isConnected);

    void onStopTrace();

    void onTraceTagEvent(int scaledRssi);

    void onClearInventoryReadings();

    void onInventoryResult(HashMap<String, String> tags, String jsonString);
}
