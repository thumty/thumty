package org.eightlog.thumty.server.params;

import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ThumbParams {

    private ThumbResize resize;

    private ThumbSize size;

    private ThumbAlign align;

    private ThumbFilters filters;

    private ThumbCrop crop;

    private ThumbTrim trim;

    private boolean signed;

    private String source;

    @Nullable
    public ThumbResize getResize() {
        return resize;
    }

    public void setResize(ThumbResize resize) {
        this.resize = resize;
    }

    @Nullable
    public ThumbSize getSize() {
        return size;
    }

    public void setSize(ThumbSize size) {
        this.size = size;
    }

    @Nullable
    public ThumbAlign getAlign() {
        return align;
    }

    public void setAlign(ThumbAlign align) {
        this.align = align;
    }

    @Nullable
    public ThumbFilters getFilters() {
        return filters;
    }

    public void setFilters(ThumbFilters filters) {
        this.filters = filters;
    }

    @Nullable
    public ThumbCrop getCrop() {
        return crop;
    }

    public void setCrop(ThumbCrop crop) {
        this.crop = crop;
    }

    @Nullable
    public ThumbTrim getTrim() {
        return trim;
    }

    public void setTrim(ThumbTrim trim) {
        this.trim = trim;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThumbParams)) return false;
        ThumbParams that = (ThumbParams) o;
        return signed == that.signed &&
                resize == that.resize &&
                Objects.equals(size, that.size) &&
                Objects.equals(align, that.align) &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(crop, that.crop) &&
                Objects.equals(trim, that.trim) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resize, size, align, filters, crop, trim, signed, source);
    }

    public String toString() {
        List<String> parts = new ArrayList<>();

        if (trim != null) {
            parts.add(trim.toString());
        }

        if (crop != null) {
            parts.add(crop.toString());
        }

        if (resize != null) {
            parts.add(resize.toString());
        }

        if (size != null) {
            parts.add(size.toString());
        }

        if (align != null) {
            parts.add(align.toString());
        }

        if (filters != null) {
            parts.add(filters.toString());
        }

        if (source != null) {
            parts.add(source);
        }

        return Joiner.on("/").join(parts);
    }

}
