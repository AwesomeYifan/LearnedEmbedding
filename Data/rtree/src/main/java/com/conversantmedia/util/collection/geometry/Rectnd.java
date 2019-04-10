package com.conversantmedia.util.collection.geometry;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.conversantmedia.util.collection.spatial.HyperPoint;
import com.conversantmedia.util.collection.spatial.HyperRect;
import com.conversantmedia.util.collection.spatial.RectBuilder;

public final class Rectnd implements HyperRect {
    private final Pointnd min, max;

    public Rectnd(final Pointnd p) {
        min = new Pointnd(p.pos);
        max = new Pointnd(p.pos);
    }

    public Rectnd(final double[] bl, final double[] tr) {
        min = new Pointnd(bl);
        max = new Pointnd(tr);
    }

    public Rectnd(final Pointnd p1, final Pointnd p2) {

        final double minx, maxx, miny, maxy, minz, maxz;
        final double[] mins = new double[p1.pos.length];
        final double[] maxs = new double[p1.pos.length];
        for(int i = 0; i < p1.pos.length; i++) {
            mins[i] = Math.min(p1.pos[i], p2.pos[i]);
            maxs[i] = Math.max(p1.pos[i], p2.pos[i]);
        }
        min = new Pointnd(mins);
        max = new Pointnd(maxs);
    }

    @Override
    public HyperRect getMbr(final HyperRect r) {
        final Rectnd r2 = (Rectnd)r;
        double[] mins = new double[min.pos.length];
        double[] maxs = new double[min.pos.length];
        for(int i = 0; i < min.pos.length; i++) {
            mins[i] = Math.min(min.pos[i], r2.min.pos[i]);
            maxs[i] = Math.max(max.pos[i], r2.max.pos[i]);
        }

        return new Rectnd(mins, maxs);

    }

    @Override
    public int getNDim() {
        return min.pos.length;
    }

    @Override
    public HyperPoint getCentroid() {
        double[] centroid = new double[min.pos.length];
        for(int i = 0; i < min.pos.length; i++) {
            centroid[i] = min.pos[i] + (max.pos[i] - min.pos[i]) / 2.0;
        }
        return new Pointnd(centroid);
    }

    @Override
    public HyperPoint getMin() {
        return min;
    }

    @Override
    public HyperPoint getMax() {
        return max;
    }

    @Override
    public double getRange(final int d) {
        if(d < min.pos.length) {
            return max.pos[d] - min.pos[d];
        }
        else {
            throw new IllegalArgumentException("Invalid dimension");
        }
    }

    @Override
    public boolean contains(final HyperRect r) {
        final Rectnd r2 = (Rectnd)r;

        boolean flag = true;
        for(int i = 0; i < min.pos.length; i++) {
            flag &= (min.pos[i] <= r2.min.pos[i] && max.pos[i] >= r2.max.pos[i]);
        }

        return flag;
    }

    @Override
    public boolean intersects(final HyperRect r) {
        final Rectnd r2 = (Rectnd)r;
        boolean flag = true;
        for(int i = 0; i < min.pos.length; i++) {
            flag &= (min.pos[i] <= r2.max.pos[i] && max.pos[i] >= r2.min.pos[i]);
        }

        return flag;
    }

    @Override
    public double cost() {
        double cumu = 1.0;
        for(int i = 0; i < min.pos.length; i++) {
            cumu *= Math.abs(max.pos[i] - min.pos[i]);
        }
        return cumu;
    }

    //why 4.0?
    @Override
    public double perimeter() {
        double coeff = Math.pow(2, min.pos.length - 1);
        double p = 0.0;
        final int nD = this.getNDim();
        for(int d = 0; d<nD; d++) {
            p += coeff * this.getRange(d);
        }
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Rectnd rectnd = (Rectnd) o;

        return min.equals(rectnd.min) &&
                max.equals(rectnd.max);
    }

    @Override
    public int hashCode() {
        return min.hashCode() ^ 31*max.hashCode();
    }

    public String toString() {
        StringBuilder rect = new StringBuilder();
        rect.append("(");
        for(int i = 0; i < min.pos.length; i++) {
            rect.append(min.pos[i]);
            rect.append(",");
            rect.append(max.pos[i]);
            rect.append(") (");
        }
        return rect.toString();
    }

    public final static class Builder implements RectBuilder<Rectnd> {

        @Override
        public HyperRect getBBox(final Rectnd rect2D) {
            return rect2D;
        }

        @Override
        public HyperRect getMbr(final HyperPoint p1, final HyperPoint p2) {
            Pointnd p1n = (Pointnd)p1;
            Pointnd p2n = (Pointnd)p2;
            return new Rectnd(p1n, p2n);
        }
    }
}