package org.eightlog.thumty.image.exif;

import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ExifUtilsTest {
    @Test
    public void exifOrientation1() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_1.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(1), orientation);
    }

    @Test
    public void exifOrientation2() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_2.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(2), orientation);
    }

    @Test
    public void exifOrientation3() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_3.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(3), orientation);
    }

    @Test
    public void exifOrientation4() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_4.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(4), orientation);
    }

    @Test
    public void exifOrientation5() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_5.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(5), orientation);
    }

    @Test
    public void exifOrientation6() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_6.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(6), orientation);
    }

    @Test
    public void exifOrientation7() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_7.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(7), orientation);
    }

    @Test
    public void exifOrientation8() throws Exception
    {
        // given
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(new File("src/test/resources/exif/orientation_8.jpg")));

        // when
        Orientation orientation = ExifUtils.getExifOrientation(reader, 0);

        // then
        assertEquals(Orientation.typeOf(8), orientation);
    }
}