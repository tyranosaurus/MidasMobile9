package com.test.midasmobile9.model;

import android.util.Log;

import com.google.gson.Gson;
import com.test.midasmobile9.data.AdminCoffeeOrderItem;
import com.test.midasmobile9.data.User;
import com.test.midasmobile9.network.NetworkDefineConstant;
import com.test.midasmobile9.network.NetworkDefineConstantCYJ;
import com.test.midasmobile9.network.OkHttpAPICall;
import com.test.midasmobile9.network.OkHttpInitSingletonManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderModel {
    private static final String TAG = "OrderModel";

    public static Map<String, Object> getOrders() {
        OkHttpClient client = OkHttpInitSingletonManager.getOkHttpClient();
        Response response = null;
        Gson gson = new Gson();

        Map<String, Object> map = null;

        try {
            response = OkHttpAPICall.GET(client, NetworkDefineConstantCYJ.SERVER_URL_GET_ORDERS);

            if ( response == null ) {
                Log.e(TAG, "Response of getOrders() is null.");

                return null;
            } else {
                JSONObject jsonFromServer = new JSONObject(response.body().string());

                // 통신결과 체크
                if ( jsonFromServer.has("result") ) {
                    map = new HashMap<String, Object>();
                    map.put("result", jsonFromServer.getBoolean("result"));
                }

                // 결과 메시지
                if ( jsonFromServer.has("message") ) {
                    map.put("message", jsonFromServer.getString("message"));
                }

                // 유저 로그인 정보
                if ( jsonFromServer.has("data") ) {
                    JSONArray jsonArray = jsonFromServer.getJSONArray("data");

                    List<AdminCoffeeOrderItem> coffeeOrderList = new ArrayList<>();

                    for ( int i = 0; i < jsonArray.length(); i++ ) {
                        AdminCoffeeOrderItem item = gson.fromJson(jsonArray.get(i).toString(), AdminCoffeeOrderItem.class);
                        coffeeOrderList.add(item);
                    }

                    map.put("coffeeOrderList", coffeeOrderList);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( response != null ) {
                response.close();
            }
        }

        return map;
    }

    public static Map<String, Object> getOrderDone() {
        OkHttpClient client = OkHttpInitSingletonManager.getOkHttpClient();
        Response response = null;
        Gson gson = new Gson();

        Map<String, Object> map = null;

        try {
            response = OkHttpAPICall.GET(client, NetworkDefineConstantCYJ.SERVER_URL_GET_ORDER_DONE);

            if ( response == null ) {
                Log.e(TAG, "Response of getOrderDone() is null.");

                return null;
            } else {
                JSONObject jsonFromServer = new JSONObject(response.body().string());

                // 통신결과 체크
                if ( jsonFromServer.has("result") ) {
                    map = new HashMap<String, Object>();
                    map.put("result", jsonFromServer.getBoolean("result"));
                }

                // 결과 메시지
                if ( jsonFromServer.has("message") ) {
                    map.put("message", jsonFromServer.getString("message"));
                }

                // 유저 로그인 정보
                if ( jsonFromServer.has("data") ) {
                    JSONArray jsonArray = jsonFromServer.getJSONArray("data");

                    List<AdminCoffeeOrderItem> orderDoneList = new ArrayList<>();

                    for ( int i = 0; i < jsonArray.length(); i++ ) {
                        AdminCoffeeOrderItem item = gson.fromJson(jsonArray.get(i).toString(), AdminCoffeeOrderItem.class);
                        orderDoneList.add(item);
                    }

                    map.put("orderDoneList", orderDoneList);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( response != null ) {
                response.close();
            }
        }

        return map;
    }

    public static Map<String, Object> updateOrderState(int orderNo, int state) {
        OkHttpClient client = OkHttpInitSingletonManager.getOkHttpClient();
        Response response = null;
        Gson gson = new Gson();

        Map<String, Object> map = null;

        RequestBody requestBody = new FormBody.Builder()
                .add("no", String.valueOf(orderNo))
                .add("state", String.valueOf(state))
                .build();

        try {
            response = OkHttpAPICall.POST(client, NetworkDefineConstantCYJ.SERVER_URL_UPDATE_ORDER_TAKEOUT, requestBody);

            if ( response == null ) {
                Log.e(TAG, "Response of updateOrderTakeOut() is null.");

                return null;
            } else {
                JSONObject jsonFromServer = new JSONObject(response.body().string());

                // 통신결과 체크
                if ( jsonFromServer.has("result") ) {
                    map = new HashMap<String, Object>();
                    map.put("result", jsonFromServer.getBoolean("result"));
                }

                // 결과 메시지
                if ( jsonFromServer.has("message") ) {
                    map.put("message", jsonFromServer.getString("message"));
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( response != null ) {
                response.close();
            }
        }

        return map;
    }
}
