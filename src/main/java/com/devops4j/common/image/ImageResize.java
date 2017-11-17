package com.devops4j.common.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Eclipse默认把这些受访问限制的API设成了ERROR。
 * 只要把Windows-Preferences-Java-Complicer-Errors/Warnings
 * 里面的Deprecated and restricted API中的Forbidden references(access rules)选为Warning就可以编译通过。
 */
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


import javax.imageio.ImageIO;

public class ImageResize {
    String source;
    String output;
    boolean verbose;

    static final String MESSAGE;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.append("图片批量缩放调整工具").append("\n");
        buffer.append(" -source(s): 图片源文件夹").append("\n");
        buffer.append(" -output(o): 图片输出文件夹").append("\n");
        buffer.append(" -width(w): 指定宽度").append("\n");
        buffer.append(" -height(h): 指定高度").append("\n");
        buffer.append(" -percent(p): 缩放比例").append("\n");
        buffer.append(" -max(m): 最大的高度或者宽度").append("\n");
        buffer.append(" -less(l): y/n 是否小于计算尺寸的图片需要进行放大").append("\n");
        buffer.append(" -verbose(v): y/n 唐生模式").append("\n");
        MESSAGE = buffer.toString();
    }


    public static void main(String[] args) throws ImageProcessingException, MetadataException, IOException {
        if (args.length == 0 || args.length % 2 != 0) {
            System.out.println(MESSAGE);
            System.exit(0);
            return;
        }
        String source = null;
        String output = null;
        int width = 0;
        int height = 0;
        double percent = -1d;
        int max = -1;
        boolean less = true;
        boolean verbose = false;
        for (int i = 0; i < args.length; i += 2) {
            String name = args[i];
            String value = args[i + 1];
            switch (name) {
                case "-source":
                case "-s":
                    source = value;
                    break;
                case "-output":
                case "-o":
                    output = value;
                    break;
                case "-width":
                case "-w":
                    width = Integer.valueOf(value);
                    break;
                case "-height":
                case "-h":
                    height = Integer.valueOf(value);
                    break;
                case "-percent":
                case "-p":
                    percent = Double.valueOf(value);
                    break;
                case "-max":
                case "-m":
                    max = Integer.valueOf(value);
                    break;
                case "-less":
                case "-l":
                    less = "y".equals(value) || "true".equals(value);
                    break;
                case "-verbose":
                case "-v":
                    verbose = "y".equals(value) || "true".equals(value);
                    if(verbose){
                        System.out.println("开始唐生模式");
                    }
                    break;
                default:
                    System.out.println(MESSAGE);
                    System.exit(0);
                    return;
            }
        }
        if(verbose){
                System.out.println("source:" + source);
                System.out.println("output:" + output);
                System.out.println("width:" + width);
                System.out.println("height:" + height);
                System.out.println("percent:" + percent);
                System.out.println("max:" + max);
                System.out.println("less:" + less);
                System.out.println("verbose:" + verbose);
        }
        ImageResize imageResize = new ImageResize(source, output, verbose);
        imageResize.resize(width, height, percent, max, less);
    }

    public ImageResize(String source, String output, boolean verbose) {
        this.source = source;
        this.output = output;
        this.verbose = verbose;
    }

    public void resize(int max, boolean less) throws Exception {
        resize(0, 0, -1, max, less);
    }

    public void resize(int width, int height, boolean less) throws Exception {
        resize(width, height, -1, -1, less);
    }

    public void resize(double zoom, boolean less) throws Exception {
        resize(0, 0, zoom, -1, less);
    }

    public void resize(int width, int height, double zoom, int max, boolean less) throws IOException, ImageProcessingException, MetadataException {
        File dir = new File(source);
        if (!dir.isDirectory()) {
            System.err.println("is not a dir");
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String fileName = name.toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpge") || fileName.endsWith(".png");
            }
        });
        File outputFile = new File(output);
        if (!outputFile.exists() && !outputFile.isDirectory()) {
            outputFile.mkdir();
        }
        int i = 0;
        for (File file : files) {
            ++i;
            int progress = (int) ((double)i / (double)files.length * 100);
            System.out.println(progress+ "%");
            if(verbose){
                System.out.println("处理文件:" + file + " (" + (i) + "/"+ files.length + ")");
            }
            resize(file, width, height, zoom, max, less);
        }

    }

    public void resize(File file, int newWidth, int newHeight, double orientation) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            resizePNG(file, newWidth, newHeight, orientation);
        } else if (fileName.endsWith("jpg") || fileName.endsWith(".jpge")) {
            resizeJPGE(file, newWidth, newHeight, orientation);
        }
    }

    public void resize(File file, int newWidth, int newHeight, double zoom, int max, boolean less) throws ImageProcessingException, IOException, MetadataException {
        double orientation = 0.0d;
        int width = 0;
        int height = 0;
        try {
            BufferedImage bi = ImageIO.read(file);

            height = bi.getHeight();
            width = bi.getWidth();
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory != null) {
                for (Tag tag : exifIFD0Directory.getTags()) {
                    if (tag.getTagName().equals("Orientation")) {
                        int val = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
//                        System.out.println(exifIFD0Directory.getDescription(ExifIFD0Directory.TAG_ORIENTATION));
                        switch (val) {
                            case 1:
//                            return "Top, left side (Horizontal / normal)";
                                orientation = 0d;
                                break;
                            case 2:
//                            return "Top, right side (Mirror horizontal)";
                                break;
                            case 3:
//                            return "Bottom, right side (Rotate 180)";
                                orientation = 180d;
                                break;
                            case 4:
//                            return "Bottom, left side (Mirror vertical)";
                                break;
                            case 5:
//                            return "Left side, top (Mirror horizontal and rotate 270 CW)";.
                                break;
                            case 6:
//                            return "Right side, top (Rotate 90 CW)";
                                orientation = 90d;
                                break;
                            case 7:
//                            return "Right side, bottom (Mirror horizontal and rotate 90 CW)";
                                break;
                            case 8:
//                            return "Left side, bottom (Rotate 270 CW)";
                                orientation = 270d;
                                break;
                            default:
//                            return String.valueOf(orientation);
                                break;
                        }
                    }
                }
            }
            if (newWidth <= 0 && newHeight <= 0) {
                if (zoom <= 0) {
                    if (max < 0) {
                        throw new RuntimeException("无效的缩放比了和固定最大宽度");
                    }
                    if (bi.getHeight() >= bi.getWidth()) {
                        if (less) {
                            zoom = (double) max / (double) height;
                        } else {
                            zoom = 1.0d;
                        }
                        newWidth = (int) (((double) bi.getWidth()) * zoom);
                        newHeight = (int) (((double) bi.getHeight()) * zoom);
                    } else {
                        if (less) {
                            zoom = (double) max / (double) width;
                        } else {
                            zoom = 1.0d;
                        }
                        newWidth = (int) (((double) bi.getWidth()) * zoom);
                        newHeight = (int) (((double) bi.getHeight()) * zoom);
                    }
                } else {
                    newWidth = (int) (((double) bi.getWidth()) * zoom);
                    newHeight = (int) (((double) bi.getHeight()) * zoom);
                }

            } else if (newWidth <= 0 && newHeight > 0) {
                zoom = (double) newHeight / (double) height;
                newWidth = (int) (((double) bi.getWidth()) * zoom);
            } else if (newWidth > 0 && newHeight <= 0) {
                zoom = (double) newWidth / (double) width;
                newHeight = (int) (((double) bi.getHeight()) * zoom);
            } else {//使用输入的高和宽

            }
            resize(file, newWidth, newHeight, orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void resizePNG(File file, int newWidth, int newHeight, double orientation) {
        try {
            BufferedImage bi = ImageIO.read(file);
            BufferedImage to = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
            Graphics2D g2d = to.createGraphics();
            to = g2d.getDeviceConfiguration().createCompatibleImage(newWidth, newHeight, Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = to.createGraphics();
            AffineTransform origXform = g2d.getTransform();
            AffineTransform newXform = (AffineTransform) (origXform.clone());
            newXform.rotate(Math.toRadians(orientation), newWidth / 2.0, newHeight / 2.0); //旋转270度
            g2d.setTransform(newXform);
            Image from = bi.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_AREA_AVERAGING);
            g2d.drawImage(from, 0, 0, null);
            g2d.dispose();
            ImageIO.write(to, "png", new File(new File(output), file.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resizeJPGE(File file, int newWidth, int newHeight, double orientation) {
        try {
            BufferedImage bi = ImageIO.read(file);
            Graphics2D g2d = bi.createGraphics();
            AffineTransform origXform = g2d.getTransform();
            AffineTransform newXform = (AffineTransform) (origXform.clone());
            newXform.rotate(Math.toRadians(orientation), newWidth / 2.0, newHeight / 2.0); //旋转270度
            BufferedImage to = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
            Graphics2D newG2d = to.createGraphics();
            newG2d.setTransform(newXform);
            newG2d.drawImage(bi, 0, 0, newWidth, newHeight, null);
            FileOutputStream out = new FileOutputStream(new File(new File(output), file.getName()));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(to);
            jep.setQuality(0.9f, true);
            encoder.encode(to, jep);
            out.close();
            bi.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}