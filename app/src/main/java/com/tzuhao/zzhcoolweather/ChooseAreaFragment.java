package com.tzuhao.zzhcoolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tzuhao.zzhcoolweather.db.City;
import com.tzuhao.zzhcoolweather.db.County;
import com.tzuhao.zzhcoolweather.db.Province;
import com.tzuhao.zzhcoolweather.util.HttpUtil;
import com.tzuhao.zzhcoolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * ChooseAreaFragment.java是LZCoolWeather的选择省市县的界面类。
 *
 * @author Learzhu
 * @version 2.2.5 2017/4/2 22:31
 * @update UserName 2017/4/2 22:31
 * @updateDes
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;

    private TextView mTitleTv;

    private Button mBackBtn;

    private ListView mListView;

    private ArrayAdapter<String> mAdapter;

    private List<String> mDataList = new ArrayList<>();

    /**
     * 选中的省份
     */
    private List<Province> mProvinceList;

    /**
     * 选中的城市
     */
    private List<City> mCityList;

    /**
     * 当前选中的城市
     */
    private List<County> mCountyList;

    /**
     * 选中的省份
     */
    private Province mSelectedProvince;

    /**
     * 选中的城市
     */
    private City mSelectedCity;

    /**
     * 当前选中的级别
     */
    private int mCurrentLevel;

    public ChooseAreaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        mTitleTv = (TextView) view.findViewById(R.id.tv_title);
        mBackBtn = (Button) view.findViewById(R.id.btn_back);
        mListView = (ListView) view.findViewById(R.id.lv_area);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinceList.get(position);
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    mSelectedCity = mCityList.get(position);
                    queryCounties();
                }
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        mTitleTv.setText("中国");
        mBackBtn.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (Province province :
                    mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        mTitleTv.setText(mSelectedProvince.getProvinceName());
        mBackBtn.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid = ?", String.valueOf(mSelectedProvince.getId())).find(City.class);
//        mCityList = DataSupport.findAll(City.class);
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city :
                    mCityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        mTitleTv.setText(mSelectedCity.getCityName());
        mBackBtn.setVisibility(View.VISIBLE);
//        mCountyList = DataSupport.findAll(County.class);
        mCountyList = DataSupport.where("cityid = ?", String.valueOf(mSelectedCity.getId())).find(County.class);
        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county :
                    mCountyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String levelType) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 通过runOnUiThread()方法回到主线程处理逻辑
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                boolean result = false;
                if ("province".equals(levelType)) {
                    result = Utility.handleProvinceResponse(responseString);
                } else if ("city".equals(levelType)) {
                    result = Utility.handleCityResponse(responseString, mSelectedProvince.getId());
                } else if ("county".equals(levelType)) {
                    result = Utility.handleCountyResponse(responseString, mSelectedCity.getId());
                }
                if (result) {
                    //如果解析成功请求省 市 县
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(levelType)) {
                                queryProvinces();
                            } else if ("city".equals(levelType)) {
                                queryCities();
                            } else if ("county".equals(levelType)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }


    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
