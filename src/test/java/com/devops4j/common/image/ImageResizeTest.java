//package com.devops4j.common.image;
//
//import org.junit.Test;
//
//import java.io.File;
//
///**
// * Created by devops4j on 2017/11/17.
// */
//public class ImageResizeTest {
//
//    @Test
//    public void testResize() throws Exception {
//        ImageResize imageResize = new ImageResize("D:/image","D:/imageout");
//        imageResize.resize(new File("D:/image/2.jpg"), 1000, 1000, -1, 0, false);
//        imageResize.resize(new File("D:/image/2.png"), 1000, 1000, -1, 0, false);
//    }
//
//    @Test
//    public void testResize1() throws Exception {
//        ImageResize resize = new ImageResize("D:/image", "D:/imageout");
//        resize.resize(1000, false);
//    }
//}