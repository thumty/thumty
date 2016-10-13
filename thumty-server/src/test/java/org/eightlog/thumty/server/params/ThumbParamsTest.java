package org.eightlog.thumty.server.params;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.atIndex;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class ThumbParamsTest {

    @Test
    public void shouldFormatParams() throws Exception {
        ThumbParams params = new ThumbParams();
        params.setSource("source");

        assertThat(params.toString()).isEqualTo("source");

        params.setTrim(new ThumbTrim(1, 1, 1));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/source");

        params.setCrop(new ThumbCrop(10, 11, 12, 13));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/source");

        params.setResize(ThumbResize.FIT);
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/fit-in/source");

        params.setSize(new ThumbSize(14, 15));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/fit-in/14.0x15.0/source");

        params.setAlign(new ThumbAlign(ThumbAlignType.FACE, 1, 0, 0));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/fit-in/14.0x15.0/face:1/source");

        params.setFilters(new ThumbFilters(Collections.singletonList(new ThumbFilter("filter", Collections.singletonList("a")))));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/fit-in/14.0x15.0/face:1/filters:filter(a)/source");
    }

    @Test
    public void shouldConvertJsonObjectToParams() throws Exception {
        JsonObject json = new JsonObject(
                                "{\n" +
                                "      \"size\": \"201x202\",\n" +
                                "      \"resize\": \"fit-in\",\n" +
                                "      \"crop\": \"10x10:20x20\",\n" +
                                "      \"trim\": \"top-left:1\",\n" +
                                "      \"align\": \"auto\",\n" +
                                "      \"filters\": [\n" +
                                "        \"blur(10)\",\n" +
                                "        \"sepia(1)\"\n" +
                                "      ]\n" +
                                "    }");

        ThumbParams params = ThumbParams.fromJson(json);

        assertThat(params.getSize()).isEqualTo(new ThumbSize(201, 202));
        assertThat(params.getResize()).isEqualTo(ThumbResize.FIT);
        assertThat(params.getCrop()).isEqualTo(new ThumbCrop(10, 10, 20, 20));
        assertThat(params.getAlign()).isEqualTo(new ThumbAlign(ThumbAlignType.AUTO));
        assertThat(params.getTrim()).isEqualTo(new ThumbTrim(0, 0, 1));
        assertThat(params.getFilters().getFilters())
                .hasSize(2)
                .contains(new ThumbFilter("blur", Collections.singletonList("10")), atIndex(0))
                .contains(new ThumbFilter("sepia", Collections.singletonList("1")), atIndex(1));
    }

    @Test
    public void shouldConvertEmptyJsonObjectToParams() throws Exception {
        JsonObject json = new JsonObject();

        ThumbParams params = ThumbParams.fromJson(json);

        assertThat(params.getSize()).isNull();
        assertThat(params.getResize()).isNull();
        assertThat(params.getCrop()).isNull();
        assertThat(params.getAlign()).isNull();
        assertThat(params.getTrim()).isNull();
        assertThat(params.getFilters()).isEmpty();
    }
}