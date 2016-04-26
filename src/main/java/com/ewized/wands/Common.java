/*
 Copyright (C) 2016 ewized

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.ewized.wands;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.year4000.utilities.utils.UtilityConstructError;

import java.util.List;

public final class Common {
    private Common() {
        UtilityConstructError.raise();
    }

    /** Create a list of vectors that will create a line between the two vector points */
    public static List<Vector3d> line(Vector3d alpha, Vector3d beta, int depth) {
        Vector3d mid = new Vector3d(
            (alpha.getX() + beta.getX()) / 2,
            (alpha.getY() + beta.getY()) / 2,
            (alpha.getZ() + beta.getZ()) / 2
        );
        List<Vector3d> collect = Lists.newArrayList(mid, alpha, beta);

        // Run at the depth
        if (depth-- > 0) {
            List<Vector3d> left = line(alpha, mid, depth);
            List<Vector3d> right = line(mid, beta, depth);

            collect.addAll(left);
            collect.addAll(right);
        }

        return collect;
    }
}
