package com.conversantmedia.util.collection.geometry;

import com.conversantmedia.util.collection.spatial.HyperPoint;
import com.conversantmedia.util.collection.spatial.HyperRect;
import com.conversantmedia.util.collection.spatial.RTree;
import com.conversantmedia.util.collection.spatial.RectBuilder;

public final class Pointnd implements HyperPoint {

    public double[] pos;

    public Pointnd(double[] pos) {
        this.pos = pos;
    }

    @Override
    public int getNDim() {
        return pos.length;
    }

    @Override
    public Double getCoord(final int d) {
        if(d < pos.length && d >= 0)
            return pos[d];
        else
            throw new IllegalArgumentException("Invalid dimension");
    }

    @Override
    public double distance(final HyperPoint p) {
        final Pointnd p2 = (Pointnd) p;
        double dist = 0.0;
        for(int i = 0; i < pos.length; i++) {
            dist += Math.pow(pos[i]-p2.pos[i], 2);
        }
        return Math.sqrt(dist);
    }

    @Override
    public double distance(final HyperPoint p, final int d) {
        final Pointnd p2 = (Pointnd)p;
        if(d < pos.length && d >= 0)
            return Math.abs(pos[d]-p2.pos[d]);
        else
            throw new IllegalArgumentException("Invalid dimension");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Pointnd p = (Pointnd)o;
        Boolean flag = true;
        for(int i = 0; i < pos.length; i++) {
            flag &= RTree.isEqual(pos[i], p.pos[i]);
        }
        return flag;
    }

    @Override
    public int hashCode() {
        int value = Integer.MAX_VALUE;
        for(int i = 0; i < pos.length; i++) {
            int temp = Double.hashCode(pos[i]) * (int)Math.pow(31,i);
            value ^= temp;
        }
        return value;
    }

    public final static class Builder implements RectBuilder<Point3d> {

        @Override
        public HyperRect getBBox(final Point3d point) {
            return new Rect3d(point);
        }

        @Override
        public HyperRect getMbr(final HyperPoint p1, final HyperPoint p2) {
            return new Rect3d((Point3d)p1, (Point3d)p2);
        }
    }
}