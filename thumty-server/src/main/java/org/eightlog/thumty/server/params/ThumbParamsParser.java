package org.eightlog.thumty.server.params;

import com.google.common.io.BaseEncoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbParamsParser {

    private final static int END = -1;
    private final static int BEGIN = 0;
    private final static int EXPECT_HMAC = 1;
    private final static int EXPECT_TRIM = 2;
    private final static int EXPECT_CROP = 3;
    private final static int EXPECT_FIT_IN = 4;
    private final static int EXPECT_SIZE = 5;
    private final static int EXPECT_ALIGN = 6;
    private final static int EXPECT_FILTERS = 7;
    private final static int EXPECT_SOURCE = 8;

    private final String url;

    private final String secret;

    private int state = BEGIN;
    private int end;

    private String value = null;

    private ThumbParamsParser(String secret, String url) {
        this.secret = secret;
        this.url = url;
    }

    public static ThumbParams parse(String secret, String url) {
        return new ThumbParamsParser(secret, url.startsWith("/") ? url : "/" + url).parse();
    }

    private ThumbParams parse() {
        ThumbParams result = new ThumbParams();

        while (state != END) {
            switch (state) {
                case BEGIN:
                    next(EXPECT_HMAC);
                    break;

                case EXPECT_HMAC:
                    if (isHmac(value)) {
                        result.setSigned(true);
                        next(EXPECT_TRIM);
                    } else {
                        state = EXPECT_TRIM;
                    }
                    break;

                case EXPECT_TRIM:
                    if (ThumbTrim.canParse(value)) {
                        result.setTrim(ThumbTrim.parse(value));
                        next(EXPECT_CROP);
                    } else {
                        state = EXPECT_CROP;
                    }
                    break;

                case EXPECT_CROP:
                    if (ThumbCrop.canParse(value)) {
                        result.setCrop(ThumbCrop.parse(value));
                        next(EXPECT_FIT_IN);
                    } else {
                        state = EXPECT_FIT_IN;
                    }
                    break;

                case EXPECT_FIT_IN:
                    if (ThumbResize.canParse(value)) {
                        result.setResize(ThumbResize.parse(value));
                        next(EXPECT_SIZE);
                    } else {
                        state = EXPECT_SIZE;
                    }
                    break;

                case EXPECT_SIZE:
                    if (ThumbSize.canParse(value)) {
                        result.setSize(ThumbSize.parse(value));
                        next(EXPECT_ALIGN);
                    } else {
                        state = EXPECT_ALIGN;
                    }
                    break;

                case EXPECT_ALIGN:
                    if (ThumbAlign.canParse(value)) {
                        result.setAlign(ThumbAlign.parse(value));
                        next(EXPECT_FILTERS);
                    } else {
                        state = EXPECT_FILTERS;
                    }
                    break;

                case EXPECT_FILTERS:
                    if (ThumbFilters.canParse(value)) {
                        result.setFilters(ThumbFilters.parse(value));
                        next(EXPECT_SOURCE);
                    } else {
                        state = EXPECT_SOURCE;
                    }
                    break;

                case EXPECT_SOURCE:
                    result.setSource(value + "/" + remains());
                    state = END;
                    break;
            }
        }

        return result;
    }

    private String remains() {
        return end < url.length() ? url.substring(end + 1) : "";
    }

    private void next(int state) {
        if (next()) {
            this.state = state;
        } else {
            this.state = END;
        }
    }

    private boolean isHmac(String value) {
        try {
            if (secret != null) {
                SecretKeySpec spec = new SecretKeySpec(BaseEncoding.base64().decode(secret), "HmacSHA1");
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(spec);

                String sign = BaseEncoding.base64Url().encode(
                        mac.doFinal(remains().getBytes(Charset.forName("UTF-8")))
                );

                return value.equals(sign);
            } else {
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean next() {
        int i = url.indexOf("/", end + 1);

        if (i == -1 && end != url.length()) {
            i = url.length();
        }

        if (i >= 0) {
            int start = end + 1;
            end = i;
            value = url.substring(start, end);
            return true;
        }

        return false;
    }

}
