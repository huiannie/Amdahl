/*
 * Copyright (c) 2015,2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.csc205.template.amdahl.engine;


import android.graphics.PointF;

public class Amdahl {
    public static final String type_SvsK = "k";
    public static final String type_SvsF = "f";
    public static final String type_default = type_SvsF;
    public static final String plotTypes[] = { type_SvsF, type_SvsK };

    private String plotType = type_default;

    private double maxK = 10d;
    public static final double minK = 1d;
    public static final double maxF = 1d;
    public static final double minF = 0d;


    public Amdahl(String plotType, double maxK) {
        if (plotType.equals(type_SvsK) || plotType.equals(type_SvsF))
            this.plotType = plotType;

        if (maxK>minK)
            this.maxK = maxK;
    }

    public void setType(String plotType) {
        if (plotType.equals(type_SvsK) || plotType.equals(type_SvsF))
            this.plotType = plotType;
    }

    public void setMaxK(double maxK) {
        if (maxK>minK)
            this.maxK = maxK;
    }

    public double getMaxX() {
        if (plotType.equals(type_SvsF))
            return maxF;
        else {
            return maxK;
        }
    }

    public double getMinX() {
        if (plotType.equals(type_SvsF))
            return minF;
        else {
            return minK;
        }
    }

    public double getMaxY() {
        return getS(maxF, maxK);
    }

    public double getMinY() {
        return getS(minF, minK);
    }



    public String getXType() {
        if (plotType.equals(type_SvsF))
            return "f";
        else
            return "k";
    }

    public String getYType() {
        return "S";
    }

    public double getS(double f, double k) {
        return 1d / ((1d-f) + f/k);
    }


    public double[] getYs(double x, int curveCount) {
        if (plotType.equals(type_SvsF)) {
            double f = x;
            double[] k = getCurveFixedParams(curveCount);
            double[] Ys = new double[curveCount];
            for (int i=0; i<curveCount; i++) {
                Ys[i] = getS(f, k[i]);
            }
            return Ys;
        }
        else {
            double k = x;
            double[] f = getCurveFixedParams(curveCount);
            double[] Ys = new double[curveCount];
            for (int i=0; i<curveCount; i++) {
                Ys[i] = getS(f[i], k);
            }
            return Ys;
        }
    }

    public String getCurveFixedParamLabel() {
        if (plotType.equals(type_SvsF)) {
            return "k";
        }
        else {
            return "f";
        }
    }

    public double[] getCurveFixedParams(int curveCount) {
        double[] params;
        if (curveCount>=0) params = new double[curveCount];
        else return null;

        if (plotType.equals(type_SvsF)) {
            if (curveCount == 1) {
                params[0] = maxK;
            }
            else {
                double intv = (maxK - minK) / (double) (curveCount);
                for (int j = 0; j < curveCount; j++) {
                    params[j] = minK + intv*(j+1);
                }
            }
        } else {
            if (curveCount == 1) {
                params[0] = maxF;
            }
            else {
                double intv = (maxF - minF) / (double) (curveCount);
                for (int j = 0; j < curveCount; j++) {
                    params[j] = minF + intv*(j+1);
                }
            }
        }
        return params;
    }

    public PointF[][] getPoints(int curveCount, int pointCount) {
        PointF[][] points;
        if (pointCount>0 && curveCount>0)
            points = new PointF[curveCount][pointCount];
        else
            points = new PointF[0][0];

        if (plotType.equals(type_SvsF)) {
            double[] k = getCurveFixedParams(curveCount);
            for (int j = 0; j < curveCount; j++) {
                for (int i = 0; i < pointCount; i++) {
                    double x = minF + (maxF - minF) / (double) (pointCount - 1) * i;
                    double y = getS(x, k[j]);
                    points[j][i] = new PointF((float) x, (float) y);
                }
            }
        }
        else {
            double[] f = getCurveFixedParams(curveCount);
            for (int j=0; j<curveCount; j++) {
                for (int i = 0; i < pointCount; i++) {
                    double x = minK + (maxK - minK) / (double) (pointCount-1) * i;
                    double y = getS(f[j], x);
                    points[j][i] = new PointF((float)x, (float) y);
                }
            }
        }
        return points;
    }


}
