package com.canyinghao.canokhttp.downpic;

import java.io.File;
import java.io.Serializable;

import static com.canyinghao.canokhttp.downpic.CanDownPicture.CONTENT_FILE_EXTENSION;
import static com.canyinghao.canokhttp.downpic.CanDownPicture.TEMP_FILE_EXTENSION;


public class PictureBean implements Serializable {


    public String dir;
    public String file;

    public String path;
    public String pathTemp;

    public PictureBean(String dir, String file) {

        this.dir = dir;
        this.file = file + CONTENT_FILE_EXTENSION;


        this.path = new File(dir, this.file).getAbsolutePath();
        this.pathTemp = new File(dir, file + TEMP_FILE_EXTENSION).getAbsolutePath();

    }
}
