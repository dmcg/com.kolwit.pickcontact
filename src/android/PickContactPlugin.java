package com.kolwit.cordova;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class PickContactPlugin extends CordovaPlugin {

    private Context context;
    private CallbackContext callbackContext;

    private static final int CHOOSE_CONTACT = 1;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.context = cordova.getActivity().getApplicationContext();

        if (!action.equals("chooseContact"))
            return false;

        Intent intent = new Intent(Intent.ACTION_PICK, kindFrom(data));
        if (!canHandle(intent)) {
            callbackContext.error("No activity found");
            return true;
        }
        cordova.startActivityForResult(this, intent, CHOOSE_CONTACT);

        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        callbackContext.sendPluginResult(r);
        return true;
    }

    private boolean canHandle(Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    private Uri kindFrom(JSONArray data) {
        try {
            if (data.length() == 0)
                return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            if (data.getString(0).equals("email"))
                return ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            if (data.getString(0).equals("none"))
                return Uri.fromParts("content", "//no.such/thing", null);
            return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        } catch (Exception x) {
            return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Uri contactData = data.getData();
            ContentResolver resolver = context.getContentResolver();
            Cursor c =  resolver.query(contactData, null, null, null, null);

            if (c.moveToFirst()) {
                try {
                    String displayName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String selectedValue = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                    JSONObject contact = new JSONObject();
                    contact.put("displayName", displayName);
                    contact.put("selectedValue", selectedValue);
                    callbackContext.success(contact);

                } catch (Exception e) {
                    callbackContext.error("Parsing contact failed: " + e.getMessage());
                }

            } else {
                callbackContext.error("Contact was not available.");
            }

            c.close();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            callbackContext.error("No contact was selected.");
        }
    }

}
