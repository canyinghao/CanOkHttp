package com.canyinghao.canokhttp.downpic;



public interface OnDownPicListener {

    public void onSuccess(String path, long size);

    public void onFail(String e);


}
