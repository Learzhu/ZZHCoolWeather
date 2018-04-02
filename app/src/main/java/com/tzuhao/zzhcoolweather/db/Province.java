package com.tzuhao.zzhcoolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Province.java是LZCoolWeather的保存省份信息的类。
 *
 * @author Learzhu
 * @version 2.2.5 2017/4/2 16:52
 * @update UserName 2017/4/2 16:52
 * @updateDes
 */
public class Province extends DataSupport {
    private int id;

    private String provinceName;

    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
