# 10 PRINT book cover generation for Java

Read more about this project in [this blog post](http://www.nypl.org/blog/2014/09/03/generative-ebook-covers).

This Java port is based on the [10 PRINT book cover generation for Python](https://github.com/mgiraldo/tenprintcover-py) project.

Java-based book cover-generating algorithm inspired by [10PRINT](http://10print.org/). Artwork uses the symbols found on the [Commodore PET](https://en.wikipedia.org/wiki/Commodore_PET) keyboard: ![Commodore PET keyboard](https://upload.wikimedia.org/wikipedia/commons/d/db/PET_Keyboard.svg)

### Requirements

Runs on Java >= 8.

Requires Noto Sans and Noto Sans CJK SC fonts installed from [Google Internationalization](https://www.google.com/get/noto/).

### Usage

To generate a book cover on your project, simply initialize a Cover instance, generate an Image and save it to the desired file name.

```java
    Cover cover = new Cover(title, subtitle, author);

    Image image = cover.draw();
    image.save(outfile);
```

Note that the default dimension of the image is set to 400×600 pixels and the file generated is a PNG.

### Other Resources

- [10 PRINT “BOOK COVER” for iOS/Objective-C](https://github.com/mgiraldo/tenprintcover-ios)
- [10 PRINT “BOOK COVER” for Processing](https://github.com/mgiraldo/tenprintcover-p5)
- [10 PRINT book cover generation for Python](https://github.com/mgiraldo/tenprintcover-py)
- [10print.org](https://10print.org/)


