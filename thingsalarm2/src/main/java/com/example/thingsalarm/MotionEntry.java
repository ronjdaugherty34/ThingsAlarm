package com.example.thingsalarm;

import java.util.Map;


class MotionEntry {

    private Long timestamp;
    private String image;
    private Map <String, Float> annotations;

    public MotionEntry () {
    }

    public MotionEntry (Long timestamp, String image, Map<String, Float> annotations) {
        this.timestamp = timestamp;
        this.image = image;
        this.annotations = annotations;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public Map<String, Float> getAnnotations() {
        return annotations;
    }

}
