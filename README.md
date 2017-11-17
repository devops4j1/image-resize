# image-resize
image resize utility.
```
java -jar image-resize.jar
图片批量缩放调整工具
 -source(s): 图片源文件夹
 -output(o): 图片输出文件夹
 -width(w): 指定宽度
 -height(h): 指定高度
 -percent(p): 缩放比例
 -max(m): 最大的高度或者宽度
 -less(l): y/n 是否小于计算尺寸的图片需要进行放大
 -verbose(v): y/n 唐生模式
```

也可以使用maven进行依赖，引入项目
```xml
<dependency>
    <groupId>com.devops4j.common</groupId>
    <artifactId>image-resize</artifactId>
    <version>1.0.0</version>
</dependency>
```
```java
ImageResize imageResize = new ImageResize(source, output, verbose);
imageResize.resize(80, true);//调整图片的高度或者宽度其中最大的为80px,小于80px的图片放大到80px
imageResize.resize(0.5, true);//调整图片的高度和宽度为一半
imageResize.resize(500, 0, true);//调整照片的高度为500，宽度按照比例调整
imageResize.resize(0, 500, true);//调整照片的宽度为500，高度按照比例调整
```
