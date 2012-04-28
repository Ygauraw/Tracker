package com.gracecode.tracker.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.MapActivity;
import com.gracecode.tracker.dao.Archive;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaiduMap extends MapActivity implements SeekBar.OnSeekBarChangeListener {
    private Archive archive;

    private Context context;
    private ArrayList<Location> locations;

    private String archiveFileName;
    private SeekBar mSeeker;
    private SimpleDateFormat dateFormat;
    private ToggleButton mSatellite;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Location location = locations.get(seekBar.getProgress() - 1);
            uiHelper.showShortToast(dateFormat.format(location.getTime()));
            setCenterPoint(location, true);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baidu_map);

        context = this;

        mapView = (MapView) findViewById(R.id.bmapsView);

        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);

        mSeeker = (SeekBar) findViewById(R.id.seek);
        mSatellite = (ToggleButton) findViewById(R.id.satellite);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        //archiveFileName = "/mnt/sdcard/tracker/201204/1334727127367.sqlite";

        dateFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.CHINA);

        archive = new Archive(getApplicationContext(), archiveFileName);
        locations = archive.fetchAll();
    }

    @Override
    public void onResume() {
//        actionBar.removeAllActions();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        int size = locations.size();
        if (size <= 0) {
            return;
        }

        mSeeker.setMax(locations.size());
        mSeeker.setProgress(0);
        mSeeker.setOnSeekBarChangeListener(this);

        mSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSatellite.isChecked()) {
                    mapView.setSatellite(true);
                } else {
                    mapView.setSatellite(false);
                }

                bMapManager.stop();
                bMapManager.start();
                uiHelper.showShortToast(getString(R.string.toggle_satellite));
            }
        });

        Location firstLocation = locations.get(0);

        Drawable marker = getResources().getDrawable(R.drawable.mark);
        mapView.getOverlays().add(new RouteItemizedOverlay(marker, context));

        //float distance = firstLocation.distanceTo(lastLocation);
        // @todo 自动计算默认缩放的地图界面
        setCenterPoint(firstLocation, false);
        mapViewController.setZoom(16);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.getOverlays().clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        archive.close();
        super.onDestroy();
    }

    class RouteItemizedOverlay extends ItemizedOverlay<OverlayItem> {
        Bitmap bitmap = null;

        private List<OverlayItem> geoPointList = new ArrayList<OverlayItem>();
        private Paint paint;

        public RouteItemizedOverlay(Drawable marker, Context context) {
            super(boundCenterBottom(marker));

            for (int i = 0; i < locations.size(); i++) {
                Location x = locations.get(i);
                GeoPoint geoPoint = getRealGeoPointFromLocation(x);
                geoPointList.add(new OverlayItem(geoPoint, x.getLatitude() + "", x.getLongitude() + ""));
            }


//            paint = new Paint();
//            paint.setAntiAlias(true);
//            paint.setColor(Color.RED);
//            paint.setAlpha(95);
//            paint.setStrokeWidth(6);

            populate();
        }

//        @Override
//        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//            Projection projection = mapView.getProjection();
//
//            GeoPoint lastGeoPoint = null;
//            int maxWidth = mapView.getWidth();
//            int maxHeight = mapView.getHeight();
//            bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
//
//            Canvas tmpCanvas = new Canvas(bitmap);
//            for (int i = 0; i < locations.size(); i++) {
//                Location x = locations.get(i);
//
//                GeoPoint geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
//                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
//
//                Point current = projection.toPixels(geoPoint, null);
//                if (lastGeoPoint != null) {
//                    Point last = projection.toPixels(lastGeoPoint, null);
//                    if (last.y < maxHeight && last.x < maxWidth) {
//                        tmpCanvas.drawLine(last.x, last.y, current.x, current.y, paint);
//                    }
//                } else {
//                    tmpCanvas.drawPoint(current.x, current.y, paint);
//                }
//
//                lastGeoPoint = geoPoint;
//            }
//
//            canvas.drawBitmap(bitmap, 0, 0, null);
//        }


        @Override
        protected OverlayItem createItem(int i) {
            return geoPointList.get(i);
        }

        @Override
        public int size() {
            return geoPointList.size();
        }


        @Override
        protected boolean onTap(int i) {
            Location location = locations.get(i);
            uiHelper.showShortToast(dateFormat.format(location.getTime()));
            mSeeker.setProgress(i);
            setCenterPoint(location, true);
            return true;
        }
    }
}
