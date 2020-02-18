/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wmsoftware_restart;

import ij.ImagePlus;
import java.util.ArrayList;

/**
 * This class that process the four moments/kinematic measures (distance,
 * velocity, acceleration and jerk).
 *
 * @author Meenakshi
 */
public class KinematicMeasures {
    //implements Measures {

    DataTrace_ver1 displacement = null;
    private DataTrace_ver1 measure = null;
    private ArrayList<Double> alongPt = null;
    private ArrayList<Double> orthoPt = null;
    private DataTrace_ver1 error = null;

    public KinematicMeasures(DataTrace_ver1 d, DataTrace_ver1 m) {
        displacement = (DataTrace_ver1) d.clone();
        this.setMeasure(m);
        this.setMeasureAlongPt(measure);
        this.setMeasureOrthoPt(measure);
        this.setMeasureError(measure);
        displacement.remove(displacement.size() - 1);
    }

    public DataTrace_ver1 getDisplacement() {
        return displacement;
    }

    public DataTrace_ver1 getMeasure() {
        return measure;
    }

    public ArrayList<Double> getMeasureAlongPt() {
        return alongPt;
    }

    public ArrayList<Double> getMeasureOrthoPt() {
        return orthoPt;
    }

    public DataTrace_ver1 getMeasureError() {
        return error;
    }

    private void setMeasure(DataTrace_ver1 m) {
        measure = Measures.getSuccessiveDifference(m);
    }

    private void setMeasureAlongPt(DataTrace_ver1 m) {
        alongPt = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            double mx = m.get(i).getX().doubleValue();
            double my = m.get(i).getY().doubleValue();
            double dx = displacement.get(i).getX().doubleValue();
            double dy = displacement.get(i).getY().doubleValue();
            double value = ((mx * dx) + (my * dy)) / (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
            alongPt.add(value);
        }
    }

    private void setMeasureOrthoPt(DataTrace_ver1 m) {
        orthoPt = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            double mx = m.get(i).getX().doubleValue();
            double my = m.get(i).getY().doubleValue();
            double dx = displacement.get(i).getX().doubleValue();
            double dy = displacement.get(i).getY().doubleValue();
            double value = ((mx * dy) - (my * dx)) / (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
            orthoPt.add(value);
        }
    }

    private void setMeasureError(DataTrace_ver1 m) {
        error = new DataTrace_ver1();
        for (int i = 0; i < m.size(); i++) {
            double mx = m.get(i).getX().doubleValue();
            double my = m.get(i).getY().doubleValue();
            double dx = displacement.get(i).getX().doubleValue();
            double dy = displacement.get(i).getY().doubleValue();
            double denom = Math.pow(dx, 2) + Math.pow(dy, 2);
            double xvalue = ((my * dx * dy) - (my * dy * dy)) / denom;
            double yvalue = ((mx * dx * dy) - (my * dx * dx)) / denom;
            error.addData(xvalue, yvalue);
        }
    }
}
