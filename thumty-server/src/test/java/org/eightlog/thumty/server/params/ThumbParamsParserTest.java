package org.eightlog.thumty.server.params;

import com.google.common.io.BaseEncoding;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbParamsParserTest {
    @Test
    public void shouldParserHMACParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/Mwty04TfQa30QOHYrrVDq3Puy4o/trim/300x200").isSigned()).isFalse();
        assertThat(ThumbParamsParser.parse("secret", "/" + hmacSha1("trim/300x200", "secret") + "/trim/300x200").isSigned()).isTrue();
    }

    @Test
    public void shouldParserTrimParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/trim").getTrim()).isEqualTo(new ThumbTrim(0, 0, 0));
        assertThat(ThumbParamsParser.parse("secret", "/trim:bottom-right").getTrim()).isEqualTo(new ThumbTrim(1, 1, 0));
        assertThat(ThumbParamsParser.parse("secret", "/trim:top-left").getTrim()).isEqualTo(new ThumbTrim(0, 0, 0));
        assertThat(ThumbParamsParser.parse("secret", "/trim:bottom-right:1").getTrim()).isEqualTo(new ThumbTrim(1, 1, 1));
        assertThat(ThumbParamsParser.parse("secret", "/trim:top-left:10").getTrim()).isEqualTo(new ThumbTrim(0, 0, 10));
        assertThat(ThumbParamsParser.parse("secret", "/trim:10").getTrim()).isEqualTo(new ThumbTrim(0, 0, 10));
        assertThat(ThumbParamsParser.parse("secret", "/" + hmacSha1("trim:top-left", "secret") + "/trim:top-left").getTrim()).isEqualTo(new ThumbTrim(0, 0, 0));
        assertThat(ThumbParamsParser.parse("secret", "/trim:something").getTrim()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/").getTrim()).isNull();
    }

    @Test
    public void shouldParserCropParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/").getCrop()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/100x100:200x200").getCrop()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/" + hmacSha1("100x100:200x200", "secret") + "/100x100:200x200").getCrop()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200").getCrop().getTop()).isEqualTo(100);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x101:200x200").getCrop().getLeft()).isEqualTo(101);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x101:201x200").getCrop().getBottom()).isEqualTo(201);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x101:201x202").getCrop().getRight()).isEqualTo(202);
        assertThat(ThumbParamsParser.parse("secret", "/trim/0.1x0.2:0.3x0.4").getCrop().getRight()).isEqualTo(0.4f);
    }

    @Test
    public void shouldParserFitInParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in").getResize()).isEqualTo(ThumbResize.FIT);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200").getResize()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/100x100:200x200/fit-in").getResize()).isEqualTo(ThumbResize.FIT);
        assertThat(ThumbParamsParser.parse("secret", "/" + hmacSha1("100x100:200x200/fit-in", "secret") + "/100x100:200x200/fit-in").getResize()).isEqualTo(ThumbResize.FIT);
        assertThat(ThumbParamsParser.parse("secret", "/fit-in").getResize()).isEqualTo(ThumbResize.FIT);
    }

    @Test
    public void shouldParserSizeParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200").getSize()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/100x200").getSize()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x200").getSize()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/100x200").getSize()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in").getSize()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200").getSize()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim").getSize()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/").getSize()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200").getSize().getWidth()).isEqualTo(100);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200").getSize().getHeight()).isEqualTo(200);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/-100x200").getSize().getWidth()).isEqualTo(-100);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x-200").getSize().getHeight()).isEqualTo(-200);
    }

    @Test
    public void shouldParserAlignParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/top").getAlign().getType()).isEqualTo(ThumbAlignType.TOP);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/100x200/bottom").getAlign().getType()).isEqualTo(ThumbAlignType.BOTTOM);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x200/bottom").getAlign().getType()).isEqualTo(ThumbAlignType.BOTTOM);
        assertThat(ThumbParamsParser.parse("secret", "/100x200/top").getAlign().getType()).isEqualTo(ThumbAlignType.TOP);
        assertThat(ThumbParamsParser.parse("secret", "/top").getAlign().getType()).isEqualTo(ThumbAlignType.TOP);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/left").getAlign().getType()).isEqualTo(ThumbAlignType.LEFT);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/100x200/right").getAlign().getType()).isEqualTo(ThumbAlignType.RIGHT);
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x200/left").getAlign().getType()).isEqualTo(ThumbAlignType.LEFT);
        assertThat(ThumbParamsParser.parse("secret", "/100x200/right").getAlign().getType()).isEqualTo(ThumbAlignType.RIGHT);
        assertThat(ThumbParamsParser.parse("secret", "/right").getAlign().getType()).isEqualTo(ThumbAlignType.RIGHT);
        assertThat(ThumbParamsParser.parse("secret", "/trim///100x100:200x200/fit-in").getAlign()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200").getAlign()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim").getAlign()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/").getAlign()).isNull();
    }

    @Test
    public void shouldParserFiltersParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg, arg2, arg3):another(arg)").getFilters()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/filters:some(arg, arg2, arg3):another(arg)").getFilters()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/filters:some(arg, arg2, arg3):another(arg)").getFilters()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/filters:some(arg, arg2, arg3):another(arg)").getFilters()).isNotNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg, arg2, arg3):another(arg)").getFilters()).isNotNull();

        assertThat(ThumbParamsParser.parse("secret", "/").getFilters()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/").getFilters()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/").getFilters()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/").getFilters()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/").getFilters()).isNull();

        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg, arg2, arg3):another(arg)").getFilters().getFilters().get(0).getArguments()).contains("arg", "arg2", "arg3");
        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg, arg2, arg3):another(arg)").getFilters().getFilters().get(1).getArguments()).contains("arg");
        assertThat(ThumbParamsParser.parse("secret", "/filters:noargs").getFilters().getFilters().get(0).getArguments()).isEmpty();
    }

    @Test
    public void shouldParserUrlParam() throws Exception {
        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg)/http://example.org/path/example/image.jpg").getSource()).isEqualTo("http://example.org/path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/filters:some(arg)/https://example.org/path/example/image.jpg").getSource()).isEqualTo("https://example.org/path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/filters:some(arg)/http://example.org/path/example/image.jpg").getSource()).isEqualTo("http://example.org/path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/filters:some(arg)/http://example.org/path/example/image.jpg").getSource()).isEqualTo("http://example.org/path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)/http://example.org/path/example/image.jpg").getSource()).isEqualTo("http://example.org/path/example/image.jpg");

        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg)/path/example/image.jpg").getSource()).isEqualTo("path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/filters:some(arg)/path/example/image.jpg").getSource()).isEqualTo("path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/filters:some(arg)/path/example/image.jpg").getSource()).isEqualTo("path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/filters:some(arg)/path/example/image.jpg").getSource()).isEqualTo("path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)/path/example/image.jpg").getSource()).isEqualTo("path/example/image.jpg");

        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)////path/example/image.jpg").getSource()).isEqualTo("///path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)/./././path/example/image.jpg").getSource()).isEqualTo("./././path/example/image.jpg");
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)/../../../path/example/image.jpg").getSource()).isEqualTo("../../../path/example/image.jpg");

        assertThat(ThumbParamsParser.parse("secret", "/trim/100x200/http://pikabu.ru/story/podruzhki_4072822").getSource()).isEqualTo("http://pikabu.ru/story/podruzhki_4072822");

        assertThat(ThumbParamsParser.parse("secret", "/" + hmacSha1("trim/100x100:200x200/fit-in/100x200/filters:some(arg)/https://example.org/path/example/image.jpg", "secret") + "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)/https://example.org/path/example/image.jpg").getSource()).isEqualTo("https://example.org/path/example/image.jpg");

        assertThat(ThumbParamsParser.parse("secret", "/filters:some(arg)/").getSource()).isEqualTo("/");
        assertThat(ThumbParamsParser.parse("secret", "/trim/filters:some(arg)").getSource()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/filters:some(arg)").getSource()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/filters:some(arg)").getSource()).isNull();
        assertThat(ThumbParamsParser.parse("secret", "/trim/100x100:200x200/fit-in/100x200/filters:some(arg)").getSource()).isNull();

    }

    private String hmacSha1(String value, String key) {

        try {
            SecretKeySpec spec = new SecretKeySpec(BaseEncoding.base64().decode(key), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(spec);

            return BaseEncoding.base64Url().omitPadding().encode(mac.doFinal(value.getBytes(Charset.forName("UTF-8"))));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}