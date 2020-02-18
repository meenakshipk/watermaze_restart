/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wmsoftware_restart;

import ij.gui.Plot;
import ij.measure.CurveFitter;
import java.util.ArrayList;
import wmsoftware_restart.DataTrace_ver1;

/**
 *
 * @author Meenakshi
 */
public interface Measures {

    /**
     * Calculate vector magnitude i.e. r
     */
    public static ArrayList<Double> getRPolarCoord(DataTrace_ver1 series) {
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            double x = series.get(i).getX().doubleValue();
            double y = series.get(i).getY().doubleValue();
            result.add(i, (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2))));
        }
        return result;
    }

    /**
     * Calculate vector angle i.e. theta
     */
    public static ArrayList<Double> getThetaPolarCoord(DataTrace_ver1 series) {
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            double x = series.get(i).getX().doubleValue();
            double y = series.get(i).getY().doubleValue();
            double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//            if (x != 0) {
//                result.add(i, Math.atan2(y, x)); //How to deal with x=0 error?
//            } else {
//                result.add(i, Double.MAX_VALUE);
//            }
            result.add(i, Math.acos(x / r));
        }
        return result;
    }

    public static DataTrace_ver1 getCartesianCoord(DataTrace_ver1 series) {
        DataTrace_ver1 result = new DataTrace_ver1();
        for (int i = 0; i < series.size(); i++) {
            double r = series.get(i).getX().doubleValue();
            double t = series.get(i).getY().doubleValue();
            double x = r * Math.cos(t);
            double y = r * Math.sin(t);
            result.addData(x, y);
        }
        return result;
    }

    public static DataTrace_ver1 getCartesianCoord(ArrayList<Double> rMag, ArrayList<Double> theta) {
        DataTrace_ver1 result = new DataTrace_ver1();
        for (int i = 0; i < rMag.size() && i < theta.size(); i++) {
            double r = rMag.get(i);
            double t = theta.get(i);
            double x = r * Math.cos(t);
            double y = r * Math.sin(t);
            result.addData(x, y);
        }
        return result;
    }

    /**
     * Calculate (x(i+1) - x(i), y(i+1) - y(i))
     *
     * @param series
     * @return
     */
    public static DataTrace_ver1 getSuccessiveDifference(DataTrace_ver1 series) {
        DataTrace_ver1 result = new DataTrace_ver1();
        for (int i = 0; i < (series.size() - 1); i++) {
            double x1 = series.get(i).getX().doubleValue();
            double x2 = series.get(i + 1).getX().doubleValue();
            double y1 = series.get(i).getY().doubleValue();
            double y2 = series.get(i + 1).getY().doubleValue();
            result.addData((x2 - x1), (y2 - y1));
        }
        return result;
    }

    public static void weightedMeanandSD(DataTrace_ver1 data) {
        int size = data.size();
        double sumNum = 0;
        double sumWeight = 0;
        for (int i = 0; i < size; i++) {
            double weight = 1 / Math.pow(data.get(i).getY().doubleValue(), 2);
            sumWeight += weight;
            sumNum += data.get(i).getX().doubleValue() * weight;
        }
        double wMean = sumNum / sumWeight;
        double sdwMean = Math.sqrt(sumWeight);
        data.addData(wMean, sdwMean);
        }
}
