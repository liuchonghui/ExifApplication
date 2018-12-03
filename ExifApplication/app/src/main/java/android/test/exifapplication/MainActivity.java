package android.test.exifapplication;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
//public class MainActivity extends Activity {

    final String RequestUrl = "http://awild.space/g/fetch_plugin";
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Point point = getDisplayPhysicalSize();
        View mainLayout = findViewById(R.id.main_layout);
        ViewGroup.LayoutParams lp = mainLayout.getLayoutParams();
        lp.height = point.y;
        mainLayout.setLayoutParams(lp);
        text = (TextView) findViewById(R.id.text);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String retJson = null;
                try {
                    Request request = new Request.Builder().url(RequestUrl).build();
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    retJson = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (retJson != null && retJson.length() > 0) {
                    final String content = retJson;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (text != null) {
                                text.setText(content);
                            }
                        }
                    });
                }
            }
        }).start();
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                String retJson = null;
//                try {
//                    Request request = new Request.Builder().url(RequestUrl).build();
//                    OkHttpClient client = new OkHttpClient();
//                    Response response = client.newCall(request).execute();
//                    retJson = response.body().string();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.d("PPP", "get|" + retJson);
//                subscriber.onNext(retJson);
//            }
//        })
//        .map(new Func1<String, JSONObject>() {
//            @Override
//            public JSONObject call(String s) {
//                JSONObject object = null;
//                try {
//                    object = new JSONObject(s);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.d("PPP", "json|" + object);
//                return object;
//            }
//        })
//        .map(new Func1<JSONObject, String>() {
//            @Override
//            public String call(JSONObject jsonObject) {
//                Log.d("PPP", "json.str|" + jsonObject.toString());
//                return jsonObject.toString();
//            }
//        })
//        .map(new Func1<String, PluginResult>() {
//            @Override
//            public PluginResult call(String s) {
//                PluginResult pluginResult = new Gson().fromJson(s, PluginResult.class);
//                Log.d("PPP", "pluginResult|" + pluginResult.state);
//                return pluginResult;
//            }
//        })
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe();

//        request(RequestUrl)
//        .flatMap(new Func1<String, Observable<JSONObject>>() {
//            @Override
//            public Observable<JSONObject> call(String s) {
//                return getJsonObject(s);
//            }
//        })
//        .flatMap(new Func1<JSONObject, Observable<String>>() {
//            @Override
//            public Observable<String> call(JSONObject jsonObject) {
//                return getJsonString(jsonObject);
//            }
//        })
//        .flatMap(new Func1<String, Observable<PluginResult>>() {
//            @Override
//            public Observable<PluginResult> call(String s) {
//                return getPluginResult(s);
//            }
//        })
//        .flatMap(new Func1<PluginResult, Observable<Plugin>>() {
//            @Override
//            public Observable<Plugin> call(PluginResult pluginResult) {
//                return getSinglePlugin(pluginResult);
//            }
//        })
//        .flatMap(new Func1<Plugin, Observable<String>>() {
//            @Override
//            public Observable<String> call(Plugin plugin) {
//                return getPluginUrl(plugin);
//            }
//        })
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe();

//        Observable.from(folders)
//                .flatMap(new Func1<File, Observable<File>>() {
//                    @Override
//                    public Observable<File> call(File file) {
//                        return Observable.from(file.listFiles());
//                    }
//                })
//                .filter(new Func1<File, Boolean>() {
//                    @Override
//                    public Boolean call(File file) {
//                        return file.getName().endsWith(".png");
//                    }
//                })
//                .map(new Func1<File, Bitmap>() {
//                    @Override
//                    public Bitmap call(File file) {
//                        return getBitmapFromFile(file);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Bitmap>() {
//                    @Override
//                    public void call(Bitmap bitmap) {
////                        imageCollectorView.addImage(bitmap);
//                    }
//                });
        String url = "http:\\/\\/124.163.221.11:80\\/play\\/CF831C8F6697AEB1F3879FE498FBD6AE404A4DC7\\/50318365.mp4?token=OTBEQjU2ODczRjBDQ0E3OUNBQkNGNzVBNjBGMjBDRENENDAwODk3MF9hcGhvbmVfMTUxNTcyNTk0MA==&vf=MCwwMUQyQg==&cp=sdkxm2&cptokenc2RreG0yLDE1MTU3MjQyMzQsNDE4MWM5NzRjYzIyZGE1NWI5YTg2M2U3OTllMDlmYmE=";
        Log.d("DDD", "url|" + url);
        String esUrl = StringEscapeUtils.escapeJava(url);
        String unesUrl = StringEscapeUtils.unescapeJava(url);
        Log.d("DDD", "StringEscapeUtils.escapeJava|" + esUrl);
        Log.d("DDD", "StringEscapeUtils.unescapeJava|" + unesUrl);
    }

    Observable<String> request(final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String retJson = null;
                try {
                    Request request = new Request.Builder().url(url).build();
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    retJson = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("PPP", "get|" + retJson);
                subscriber.onNext(retJson);
            }
        });
    }

    Observable<JSONObject> getJsonObject(final String input) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                JSONObject object = null;
                try {
                    object = new JSONObject(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("PPP", "json|" + object);
                subscriber.onNext(object);
            }
        });
    }

    Observable<String> getJsonString(final JSONObject jObj) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d("PPP", "json.str|" + jObj.toString());
                String str = jObj.toString();
                subscriber.onNext(str);
            }
        });
    }

    Observable<PluginResult> getPluginResult(final String str) {
        return Observable.create(new Observable.OnSubscribe<PluginResult>() {
            @Override
            public void call(Subscriber<? super PluginResult> subscriber) {
                PluginResult pluginResult = new Gson().fromJson(str, PluginResult.class);
                Log.d("PPP", "pluginResult|" + pluginResult.state);
                subscriber.onNext(pluginResult);
            }
        });
    }

    Observable<Plugin> getSinglePlugin(final PluginResult pluginResult) {
        return Observable.from(pluginResult.getPlugins());
    }

    Observable<String> getPluginUrl(final Plugin plugin) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String url = plugin.url;
                Log.d("PPP", "getPluginUrl|" + plugin.id + "|url|" + plugin.url);
                subscriber.onNext(url);
            }
        });
    }

    Point getDisplayPhysicalSize() {
        Point result = new Point();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Display.Mode mode = display.getMode();
            int width = -1;
            int height = -1;
            if (mode != null) {
                width = mode.getPhysicalWidth();
                height = mode.getPhysicalHeight();
            }
            if (width > 0 && height > 0) {
                result.x = width;
                result.y = height;
            } else {
                display.getRealSize(result);
            }
        } else {
            display.getRealSize(result);
        }
        return result;
    }
}
