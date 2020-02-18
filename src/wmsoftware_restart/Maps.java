/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wmsoftware_restart;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * 5 output maps can be made using measure - i.e. vector map, div, curl, heat
 * map, gradient
 *
 * @author Meenakshi
 */
public abstract class Maps {

    int pX = 175;
    int pY = 175;
    int dimX = 240;
    int dimY = 240;
    DataTrace_ver1 position = null;
    float[][] resTimeArr = null;
    Roi resROI = null;
    DataTrace_ver1 measure = null;
    HashMap<String, Object> mapResultsObject = null;

    public Maps(DataTrace_ver1 p) {
        position = p;
        resTimeArr = this.residenceTimeCounts(position);
        ImagePlus impResTime = this.generateMap(resTimeArr);
        this.setResidenceROI(impResTime);
    }
//    @Override
//    public Maps clone() throws CloneNotSupportedException {
//                return (Maps) super.clone();
//        }

    public void setDimensions(int xDim, int yDim) {
        dimX = xDim;
        dimY = yDim;
    }

    public void setPlatform(int xP, int yP) {
        pX = xP;
        pY = yP;
    }

    public void setMeasure(DataTrace_ver1 m) {
        measure = m;
    }

    public HashMap<String, Object> getMapResults() {
        return mapResultsObject;
    }

    public void setMapResults(ImagePlus imp) {
        mapResultsObject = new HashMap<>();
        ImageProcessor ip = imp.getProcessor();
        this.RmFromMap(ip);
        this.quadrantandZoneMeasures(imp);
    }

    /**
     *
     * @param rM
     */
    public void setMeasure(ArrayList<Double> rM) {
    }

    public DataTrace_ver1 getMeasure() {
        return measure;
    }

    /**
     * Calculate residence time
     */
    private float[][] residenceTimeCounts(DataTrace_ver1 series) {
        float[][] resTime = new float[dimX][dimY];
        for (int i = 0; i < series.size(); i++) {
            int XPo = (int) Math.round(series.get(i).getX().doubleValue());
            int YPo = (int) Math.round(series.get(i).getY().doubleValue());
            resTime[XPo][YPo] = resTime[XPo][YPo] + 1;
        }
        return resTime;
    }

    /**
     * calculate each pixel value given xy data points in DataTrace_ver1
     *
     * @param curSeries
     * @param M
     * @return
     */
    final float[][] sumPixelsImage(DataTrace_ver1 curSeries, ArrayList<Double> M) {
        float[][] ipArray = new float[dimX][dimY];

        int size = M.size();
        for (int j = 0; j < size; j++) {
            double XPo = curSeries.get(j).getX().doubleValue();
            double YPo = curSeries.get(j).getY().doubleValue();
            int xPix = (int) Math.round(XPo);
            int yPix = (int) Math.round(YPo);
            ipArray[xPix][yPix] = (float) (ipArray[xPix][yPix] + M.get(j));
            //NOTE: CANNOT USE NANs WHEN USING SurfaceFit/Differentials plugin
            //Make NaN image during resize for div,curl,grad. Use ipResROI for heat map
        }
        return ipArray;
    }

    /**
     * generate residence time weighted heat map
     *
     * @param resTime
     * @param measure
     * @return
     */
    final float[][] resTimeWeighted(float[][] resTime, float[][] measure) {

        float[][] image = new float[dimX][dimY];
        for (int Y = 0; Y < dimY; Y++) {
            for (int X = 0; X < dimX; X++) {
                if (resTime[X][Y] != 0) {
                    image[X][Y] = (float) (measure[X][Y] / resTime[X][Y]);
                } else {
                    image[X][Y] = 0f;
                }
            }
        }
        return image;
    }

    /**
     * generate imageplus using float array
     *
     * @param floatArr
     * @return
     */
    final ImagePlus generateMap(float[][] floatArr) {
        ImageProcessor ip = new FloatProcessor(dimX, dimY);
        ip.setFloatArray(floatArr);
        ImagePlus result = new ImagePlus();
        result.setProcessor(ip);
        return result;
    }

    /**
     * create ROI selection
     */
    private void setResidenceROI(ImagePlus impResTime) {
        ImageProcessor ipResTime = impResTime.getProcessor();
        //threshold out 0 values and create an ROI in restime image
        double minValue = 1;
        double maxValue = ipResTime.getMax();
        ipResTime.setThreshold(minValue, maxValue, 3);
        ThresholdToSelection tts = new ThresholdToSelection();
        Roi result = tts.convert(ipResTime);
        resROI = result;
    }

    /**
     * generate polynomial surface fit of order 3
     *
     * @param image
     * @return
     */
    ImagePlus thresholdedSurfaceFit3(ImagePlus image) {
        //polynomial fit function -
        Polynomial_Surface_Fit psf = new Polynomial_Surface_Fit(image);
        image = psf.run(image.getProcessor());
        return image;
    }

    /**
     * calculates Rm value from vector/scalar property map
     *
     * @param ipResTime
     * @param ip
     * @return
     */
    private void RmFromMap(ImageProcessor ip) {
        double[] RmArray = new double[2];
        Rectangle bounds = resROI.getBounds();
//      System.out.println("Bounding rect" + bounds);
        //Find coordinates within the pool ROI
        OvalRoi pool = new OvalRoi(0, 0, 240, 240);
        MaximumFinder mf = new MaximumFinder();
        //find coordinates of maxima and minima points
        Polygon[] pixelList = new Polygon[2];
        Polygon points = mf.getMaxima(ip, 0.00001, true); //excludes edges
        pixelList[0] = points; //maxima at idx[0]
        ip.invert();
        points = mf.getMaxima(ip, 0.00001, true); //excludes edges
        pixelList[1] = points; //minima at idx[1]
        for (int i = 0; i < pixelList.length; i++) {
            ArrayList<Double> RmList = new ArrayList<>();
            ArrayList<Float> intensity = new ArrayList<>();
            int xb = (int) bounds.getX();
            int yb = (int) bounds.getY();
            Polygon pixel = pixelList[i];
            for (int ii = 0; ii < pixel.npoints; ii++) {
                int X = pixel.xpoints[ii] + xb;
                int Y = pixel.ypoints[ii] + yb;
                if (pool.containsPoint(X, Y)) {
                    intensity.add(ip.getPixelValue(pixel.xpoints[ii], pixel.ypoints[ii]));
                    double Rm = Math.sqrt(Math.pow((pX - X), 2) + Math.pow((pY - Y), 2));
                    RmList.add(Rm);
                }
            }
            double Rm;
            try {
                float max_intensity = Collections.max(intensity);
                int index = intensity.indexOf(max_intensity);
                Rm = RmList.get(index);
            } catch (NoSuchElementException c) {
                Rm = Double.NaN;
            }
            RmArray[i] = Rm;
        }
//        System.out.println(Arrays.toString(RmArray));
        OrdXYData result = new OrdXYData(0, RmArray[0], RmArray[1]); //maxima, minima
        mapResultsObject.put("Rm", result);
//        return result;
    }

    /**
     * resize surface fit-differential images to dimX by dimY dimensions
     *
     * @param ipResTime
     * @param ip
     * @return
     */
    ImagePlus resizeImage(ImageProcessor ip) {
        ImagePlus imp = new ImagePlus();
        OvalRoi pool = new OvalRoi(0, 0, 240, 240);

        Rectangle bounds = resROI.getBounds();

//      resize image to 240 by 240 dimension
        float[][] processedArray = new float[dimX][dimY];
        for (int Y = 0; Y < dimY; Y++) {
            for (int X = 0; X < dimX; X++) {
                processedArray[X][Y] = Float.NaN;
//                processedArray[X][Y] = 0;
            }
        }
        int xb = (int) bounds.getX();  //already defined above
        int yb = (int) bounds.getY();  // already defined above
        int xbmax = (int) bounds.getWidth();
        int ybmax = (int) bounds.getHeight();
        float[][] f = ip.getFloatArray();
        for (int Y = 0; Y < ybmax; Y++) {
            for (int X = 0; X < xbmax; X++) {
                if (pool.containsPoint(X + xb, Y + yb)) {
                    processedArray[X + xb][Y + yb] = f[X][Y];
                }
            }
        }
        ImageProcessor processedip = new FloatProcessor(processedArray);
        imp.setProcessor(processedip);
        return imp;
    }

    private void quadrantandZoneMeasures(ImagePlus imp) {
        int iWidth = (int) dimX / 2;
        int iHeight = (int) dimY / 2;
        int iXROI = 0;
        int iYROI = 0;
        Roi[] quadROIs = new Roi[4];
        quadROIs[0] = new Roi(iXROI, iYROI, iWidth, iHeight); //Q0 - top left
        quadROIs[1] = new Roi(iXROI + iWidth, iYROI, iWidth, iHeight); //Q1 - top right
        quadROIs[2] = new Roi(iXROI + iWidth, iYROI + iHeight, iWidth, iHeight); //Q2 - bottom right
        quadROIs[3] = new Roi(iXROI, iYROI + iHeight, iWidth, iHeight); //Q3 - bottom left
        RoiManager roiMan = new RoiManager(false);
        int quadNo = -1;
        for (int i = 0; i < quadROIs.length; i++) {
            Roi quad = quadROIs[i];
            roiMan.addRoi(quad);
            boolean platQuad = quad.contains(pX, pY);
            if (platQuad) {
                quadNo = i;
            }
        }
        OvalRoi[] zoneROIs = new OvalRoi[4];
        iWidth = 20;
        iHeight = 20;
        int x0 = 0, y0 = 0, x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;
        switch (quadNo) {
            //quadrants labelled from 0 to 3 in clockwise direction
            case 0: //quad 0
                x0 = pX - (iWidth / 2);
                y0 = pY - (iHeight / 2);
                x1 = x0 + (dimX / 2);
                y1 = y0;
                x2 = x1;
                y2 = y0 + (dimY / 2);
                x3 = x0;
                y3 = y2;
                break;
            case 1: //quad 1
                x1 = pX - (iWidth / 2);
                y1 = pY - (iHeight / 2);
                x0 = x1 - (dimX / 2);
                y0 = y1;
                x2 = x1;
                y2 = y1 + (dimY / 2);
                x3 = x0;
                y3 = y2;
                break;
            case 2: //quad 2
                x2 = pX - (iWidth / 2);
                y2 = pY - (iHeight / 2);
                x0 = x2 - (dimX / 2);
                y0 = y2 - (dimY / 2);
                x1 = x2;
                y1 = y0;
                x3 = x0;
                y3 = y2;
                break;
            case 3: //quad 3
                x3 = pX - (iWidth / 2);
                y3 = pY - (iHeight / 2);
                x0 = x3;
                y0 = y3 - (dimY / 2);
                x1 = x3 - (dimX / 2);
                y1 = y0;
                x2 = x1;
                y2 = y3;
                break;
            default:
                System.out.println("Platform coordinates are not within pool dimensions");
                break;
        }
        zoneROIs[0] = new OvalRoi(x0, y0, iWidth, iHeight);
        zoneROIs[1] = new OvalRoi(x1, y1, iWidth, iHeight);
        zoneROIs[2] = new OvalRoi(x2, y2, iWidth, iHeight);
        zoneROIs[3] = new OvalRoi(x3, y3, iWidth, iHeight);
        for (OvalRoi zone : zoneROIs) {
            roiMan.addRoi(zone);
        }
        //set mean measurement, multimeasure using 8 rois in roimanager, get results table
        Analyzer.setMeasurements(Measurements.MEAN + Measurements.STD_DEV);
        ResultsTable rt = roiMan.multiMeasure(imp);
//        rt.show("");
        DataTrace_ver1 q = new DataTrace_ver1();
        for (int i = 0; i < rt.getLastColumn() / 2; i += 2) {
            q.addData(rt.getValueAsDouble(i, 0), rt.getValueAsDouble(i + 1, 0));
        }
        mapResultsObject.put("QuadrantMeasure", q);
        DataTrace_ver1 z = new DataTrace_ver1();
        for (int i = (rt.getLastColumn() / 2) + 1; i < rt.getLastColumn(); i += 2) {
            z.addData(rt.getValueAsDouble(i, 0), rt.getValueAsDouble(i + 1, 0));
        }
        mapResultsObject.put("ZoneMeasure", z);
//        return qp;
    }
}

class VectorMaps extends Maps {

    ImagePlus xImage = null, yImage = null;

    public VectorMaps(DataTrace_ver1 p) {
        super(p);
    }

    @Override
    public void setMeasure(DataTrace_ver1 m) {
        super.setMeasure(m);
        //create image
        float[][] xImageArr = super.sumPixelsImage(position, measure.getX());
        float[][] yImageArr = super.sumPixelsImage(position, measure.getY());
        //ResTime weighted
        xImageArr = super.resTimeWeighted(resTimeArr, xImageArr);
        yImageArr = super.resTimeWeighted(resTimeArr, yImageArr);
        //maps
        xImage = super.generateMap(xImageArr);
        yImage = super.generateMap(yImageArr);
    }

    /**
     * generate vector map
     *
     * @param resultName
     * @return
     */
    public ImagePlus vectorField() {
        ImageProcessor xImg = (ImageProcessor) xImage.getProcessor().duplicate();
        ImageProcessor yImg = (ImageProcessor) yImage.getProcessor().duplicate();

        double[] xBegin = new double[dimX * dimY];
        double[] yBegin = new double[dimX * dimY];
        double[] xEnd = new double[dimX * dimY];
        double[] yEnd = new double[dimX * dimY];

        for (int Y = 0; Y < dimY; Y++) {
            for (int X = 0; X < dimX; X++) {
                int arrayIdx = (Y * dimX) + X;
                float xPixelValue = xImg.getPixelValue(X, Y);
                float yPixelValue = yImg.getPixelValue(X, Y);
                if (xPixelValue != 0 && yPixelValue != 0) {
                    xBegin[arrayIdx] = X;
                    yBegin[arrayIdx] = Y;
                    xEnd[arrayIdx] = xPixelValue + X;
                    yEnd[arrayIdx] = yPixelValue + Y;
                }
            }
        }
        Plot vectorField = new Plot("", "X Axis", "Y Axis");
        vectorField.setLimits(0, dimX, 0, dimY);
        vectorField.drawVectors(xBegin, yBegin, xEnd, yEnd);
//        vectorField.show();
        ImagePlus result = vectorField.getImagePlus();
        return result;
    }

    /**
     * generate divergence
     *
     * @return
     */
    public ImagePlus divergence() {
        ImagePlus xImg = xImage.duplicate();
        ImagePlus yImg = yImage.duplicate();
        //set ROI
        xImg.setRoi(resROI);
        yImg.setRoi(resROI);
        //surface fit
        xImg = super.thresholdedSurfaceFit3(xImg);
        yImg = super.thresholdedSurfaceFit3(yImg);
        //differentiate image
        Differentials_JB diffJB = new Differentials_JB();
        diffJB.run2(xImg, 6); //differentiate xImg wrt x        
        diffJB.run2(yImg, 7); //differentiate yImg wrt y
        //add images
        yImg.getProcessor().copyBits(xImg.getProcessor(), 0, 0, Blitter.ADD);
        //resize image
        yImg = super.resizeImage(yImg.getProcessor());
        return yImg;
    }

    /**
     * generate curl
     *
     * @return
     */
    public ImagePlus curl() {
        ImagePlus xImg = xImage.duplicate();
        ImagePlus yImg = yImage.duplicate();
        //set ROI
        xImg.setRoi(resROI);
        yImg.setRoi(resROI);
        //surface fit
        xImg = super.thresholdedSurfaceFit3(xImg);
        yImg = super.thresholdedSurfaceFit3(yImg);
        //differentiate image
        Differentials_JB diffJB = new Differentials_JB();
        diffJB.run2(yImg, 6); //differentiate yImg wrt x
        diffJB.run2(xImg, 7); //differentiate xImg wrt y        
        //subtract images
        yImg.getProcessor().copyBits(xImg.getProcessor(), 0, 0, Blitter.DIFFERENCE);
        //resize image
        yImg = super.resizeImage(yImg.getProcessor());
        return yImg;
    }
}

class ScalarMaps extends Maps {

    ImagePlus image = null;

    public ScalarMaps(DataTrace_ver1 p) {
        super(p);
    }

    @Override
    public void setMeasure(DataTrace_ver1 m) {
        super.setMeasure(m);
        ArrayList<Double> rMeasure = Measures.getRPolarCoord(measure);
        this.setMeasure(rMeasure);
    }

    @Override
    public void setMeasure(ArrayList<Double> rMeasure) {
        //create imageArr
        float[][] ImageArr = this.sumPixelsImage(position, rMeasure);
        //ResTime weighted
        ImageArr = this.resTimeWeighted(resTimeArr, ImageArr);
        //maps
        image = this.generateMap(ImageArr);
    }

    public ImagePlus heatMap() {
        ImagePlus Image = (ImagePlus) image.duplicate();
        Image.getProcessor().setColor(Float.NaN);
        Image.getProcessor().fillOutside(resROI);
        return Image;
    }

    /**
     * generate gradient
     *
     * @return
     */
    public ImagePlus gradient() {
        ImagePlus Image = (ImagePlus) image.duplicate();
        //set ROI
        Image.setRoi(resROI);
        //surface fit
        Image = super.thresholdedSurfaceFit3(Image);
        //differentiate image
        Differentials_JB diffJB = new Differentials_JB();
        diffJB.run2(Image, 0); //gradient magnitude
        //resize image
        Image = super.resizeImage(Image.getProcessor());
        return Image;
    }
}
