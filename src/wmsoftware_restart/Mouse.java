/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wmsoftware_restart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Meenakshi
 */
public class Mouse {

    private String mouseID = null;
    private String trialID = null;
    private File dataFile = null;
    private DataTrace_ver1 position = null;
    private DataTrace_ver1 displacement = null;
    private HashMap<String, KinematicMeasures> kinematicMeasures = null;
//    KinematicMeasures velocity = null;
//    KinematicMeasures acceleration = null;
//    KinematicMeasures jerk = null;
    private HashMap<String, HashMap<String, Object>> kinematicMeasuresMapProperties = null;

    int pX = 175; //later make it into another object? like experiment or platform or pool?
    int pY = 175; //later make it into another object? like experiment or platform or pool?

    public Mouse(String trial, String mouse, File file) {
        trialID = trial;
        mouseID = mouse;
        dataFile = file;
        position = this.readFile();
        kinematicMeasures = new HashMap<>();
    }

    public String getTrialID() {
        return trialID;
    }

    public String getMouseID() {
        return mouseID;
    }

    public DataTrace_ver1 getPosition() {
        return position;
    }

    public HashMap<String, KinematicMeasures> getKinematicMeasures() {
        return kinematicMeasures;
    }

    private KinematicMeasures getKinematicMeasures(String measure) {
        KinematicMeasures result = null;
        if (kinematicMeasures.containsKey(measure)) {
            result = kinematicMeasures.get(measure);
        }
        return result;
    }

    public KinematicMeasures getVelocity() {
        return this.getKinematicMeasures("Velocity");
    }

    public KinematicMeasures getAcceleration() {
        return this.getKinematicMeasures("Acceleration");
    }

    public KinematicMeasures getJerk() {
        return this.getKinematicMeasures("Jerk");
    }

    public HashMap<String, Object> getMapProperties(String kinematicMeasure) {
        HashMap<String, Object> mapProperties = null;
        if (kinematicMeasuresMapProperties.containsKey(kinematicMeasure)) {
            kinematicMeasuresMapProperties.get(kinematicMeasure);
        }
        return mapProperties;
    }

    private Object getMapProperty(String kinematicMeasure, String mapProperty) {
        HashMap<String, Object> mapProperties = this.getMapProperties(kinematicMeasure);
        Object result = null;
        if (mapProperties.containsKey(mapProperty)) {
            result = (OrdXYData) mapProperties.get(mapProperty);
        }
        return result;
    }

    public OrdXYData getMapPropertyRm(String kinematicMeasure) {
        OrdXYData result = (OrdXYData) this.getMapProperty(kinematicMeasure, "Rm");
        return result;
    }

    public DataTrace_ver1 getMapPropertyQuadrant(String kinematicMeasure) {
        DataTrace_ver1 result = (DataTrace_ver1) this.getMapProperty(kinematicMeasure, "QuadrantMeasure");
        return result;
    }

    public DataTrace_ver1 getMapPropertyZone(String kinematicMeasure) {
        DataTrace_ver1 result = (DataTrace_ver1) this.getMapProperty(kinematicMeasure, "ZoneMeasure");
        return result;
    }

    public void setMapProperties(String kinematicMeasure, HashMap<String, Object> mp) {
        kinematicMeasuresMapProperties = new HashMap<>();
        kinematicMeasuresMapProperties.put(kinematicMeasure, mp);
    }

    public void setVelocity() {
        if (displacement == null) {
            this.setDisplacement();
        }
        KinematicMeasures velocity = new KinematicMeasures(displacement, displacement);
        kinematicMeasures.put("Velocity", velocity);
    }

    public void setAcceleration() {
        KinematicMeasures velocity = kinematicMeasures.get("Velocity");
        if (velocity == null) {
            this.setVelocity();
        }
        DataTrace_ver1 dis = velocity.getDisplacement();
        DataTrace_ver1 vel = velocity.getMeasure();
        KinematicMeasures acceleration = new KinematicMeasures(dis, vel);
        kinematicMeasures.put("Acceleration", acceleration);
    }

    public void setJerk() {
        KinematicMeasures acceleration = kinematicMeasures.get("Acceleration");
        if (acceleration == null) {
            this.setAcceleration();
        }
        DataTrace_ver1 dis = acceleration.getDisplacement();
        DataTrace_ver1 acc = acceleration.getMeasure();
        KinematicMeasures jerk = new KinematicMeasures(dis, acc);
        kinematicMeasures.put("Jerk", jerk);
    }

    /**
     * calculate displacement vector from the platform location
     */
    private void setDisplacement() {
        displacement = new DataTrace_ver1();
        for (int i = 0; i < position.size(); i++) {
            double x = position.get(i).getX().doubleValue();
            double y = position.get(i).getY().doubleValue();
            displacement.addData((pX - x), (pY - y));
        }
    }

    private DataTrace_ver1 readFile() {
        //reading the file and saving it to DataTrace
        String dataString = "";
        int c = 0;
        double xData = 0;
        double yData = 0;
        FileReader fReader = null; //Reader class : Java class for reading text files (ASCII)
        DataTrace_ver1 series = new DataTrace_ver1(); //returns the series read at the end

        if (dataFile.exists()) {
            try {
                fReader = new FileReader(dataFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Mouse.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                while ((c = fReader.read()) != -1) {
                    switch (c) {
                        case '\t':
                            xData = Double.parseDouble(dataString);
                            dataString = "";
                            break;
                        case '\n':
                            yData = Double.parseDouble(dataString);
                            series.addData(xData, yData);
                            dataString = "";
                            break;
                        default:
                            dataString += (char) c;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Mouse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return series;
    }
}
