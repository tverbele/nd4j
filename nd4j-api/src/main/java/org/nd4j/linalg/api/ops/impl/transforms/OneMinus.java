/*
 * Copyright 2015 Skymind,Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.nd4j.linalg.api.ops.impl.transforms;

import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.BaseTransformOp;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.factory.Nd4j;

/**
 * 1 - input
 *
 * @author Adam Gibson
 */
public class OneMinus extends BaseTransformOp {
    public OneMinus(INDArray x, INDArray z) {
        super(x, z);
    }

    public OneMinus(INDArray x, INDArray z, int n) {
        super(x, z, n);
    }

    public OneMinus(INDArray x, INDArray y, INDArray z, int n) {
        super(x, y, z, n);
    }

    public OneMinus(INDArray x) {
        super(x);
    }

    @Override
    public String name() {
        return "oneminus";
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, double other) {
        return Nd4j.createComplexNumber(1, 1).subi(origin);
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, float other) {
        return Nd4j.createComplexNumber(1, 1).subi(origin);

    }

    @Override
    public IComplexNumber op(IComplexNumber origin, IComplexNumber other) {
        return Nd4j.createComplexNumber(1, 1).subi(origin);

    }

    @Override
    public float op(float origin, float other) {
        return 1 - origin;
    }

    @Override
    public double op(double origin, double other) {
        return 1 - origin;
    }

    @Override
    public double op(double origin) {
        return 1 - origin;
    }

    @Override
    public float op(float origin) {
        return 1 - origin;
    }

    @Override
    public IComplexNumber op(IComplexNumber origin) {
        return Nd4j.createComplexNumber(1, 1).subi(origin);

    }

    @Override
    public Op opForDimension(int index, int dimension) {
        INDArray xAlongDimension = x.vectorAlongDimension(index, dimension);

        if (y() != null)
            return new OneMinus(xAlongDimension, y.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length());
        else
            return new OneMinus(xAlongDimension, z.vectorAlongDimension(index, dimension), x.length());

    }
}
