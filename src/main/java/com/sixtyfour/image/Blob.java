package com.sixtyfour.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A blob with a timestamp. Can also store a list of image names.
 *
 * @author EgonOlsen
 */
public class Blob {

    private long time;

    private byte[] data;

    private String target;

    private String source;

    private String error;

    private List<String> images;

    public Blob(List<String> images) {
        time = System.currentTimeMillis();
        this.images = new ArrayList<>(images);
    }

    public Blob(String target, String source) {
        time = System.currentTimeMillis();
        this.target = target;
        this.source = source;
    }

    public Blob(String error) {
        time = System.currentTimeMillis();
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isError() {
        return error!=null;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getTarget() {
        return target;
    }

    public boolean isOld(long maxTime) {
        return System.currentTimeMillis()-time>=maxTime;
    }

    public int getAge() {
        return (int) (System.currentTimeMillis()-time)/1000;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ByteArrayInputStream getAsStream() {
        return new ByteArrayInputStream(data);
    }

    public List<String> getImages() {
        return new ArrayList<>(images);
    }

    public void setImages(List<String> images) {
        this.images = new ArrayList<>(images);
    }

    public void fill(InputStream is) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            is.transferTo(bos);
            data = bos.toByteArray();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
