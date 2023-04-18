package org.ocr.sdk.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Point implements Comparable<Point> {
    Double x;
    Double y;

    String msg;

    public Point() {
    }

    public Point(Double x, Double y, String msg) {
        this.x = x;
        this.y = y;
        this.msg = msg;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("x", x)
                .append("y", y)
                .append("msg", msg)
                .toString();
    }

    @Override
    public int compareTo(Point o) {
        if (this.getY() < o.getY() ) {
            return -1;
        } else if ((this.getY() - o.getY()) <= 0.01) {
            return this.getX() < o.getX() ? -1 : 1;
        } else {
            return 1;
        }

    }
}
