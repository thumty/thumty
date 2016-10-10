package org.eightlog.thumty.server.params;

import org.junit.Test;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

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

        params.setFilters(new ThumbFilters(Collections.singletonMap("filter", new ThumbFilter("filter", Collections.singletonList("a")))));
        assertThat(params.toString()).isEqualTo("trim:bottom-right:1/10.0x11.0:12.0x13.0/fit-in/14.0x15.0/face:1/filters:filter(a)/source");

    }
}