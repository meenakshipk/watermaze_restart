/**
 *
 * @author J Balaji
 * Meenakshi P
 *
 *
 * DataTrace_ver1 is a java class for use instead of XYSeries in jFreeChart library
 */
// Modified on 31.05.2019 - added JavaDoc
// Modified on 2019.12.14 - completed adding javadoc. Proof read binInX code.
package wmsoftware_restart;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
/*import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.*;*/
import java.awt.datatransfer.*;
import java.sql.Array;
import javafx.collections.transformation.SortedList;
import javax.swing.*;

class OrdXYData<X extends Number, Y extends Number> extends Object {

    int serialNo;
    X xDataPt;
    Y yDataPt;

    /**
     * creates a new object of ordinary data of x,y data columns
     *
     * @param serial the integer representation of serial number
     * @param x the number object representation of X column
     * @param y the number object representation of Y column
     */
    public OrdXYData(int serial, X x, Y y) {
        xDataPt = x;
        yDataPt = y;
        serialNo = serial;
    }

    /**
     * get the x data point
     *
     * @return x data point as an object of Number class
     */
    public X getX() {
        return xDataPt;
    }

    /**
     * get the y data point
     *
     * @return y data point as an object of Number class
     */
    public Y getY() {
        return yDataPt;
    }

    /**
     * makes an ArrayList collection of XY data
     *
     * @return XY data in an ArrayList form
     */
    public ArrayList<? extends Number> getXY() {
        ArrayList dataArray = new ArrayList(2);
        dataArray.add(xDataPt);
        dataArray.add(yDataPt);
        return dataArray;
    }

    /**
     * get serial number of data
     *
     * @return serial number as an integer
     */
    public int getSerial() {
        return serialNo;
    }
}

/**
 * DataTrace_ver1 is a java class for use instead of XYSeries in jFreeChart
 * library
 *
 * @author J Balaji Meenakshi P (20191214)
 *
 */
public class DataTrace_ver1 extends ArrayList<OrdXYData> {

    //ArrayList<OrdXYData> rawData;
    double x_Max = Double.MIN_VALUE;
    double y_Max = Double.MIN_VALUE;
    double x_Min = Double.MAX_VALUE;
    double y_Min = Double.MAX_VALUE;
    double x_Sum = 0;
    double y_Sum = 0;
    int CurrPos = 0;
    int DataLength = 0;
    int ActLength = 0;                       //Needs comment : to say what is the difference between DataLength and ActLength
    // Actlength - the number of datapoints that are non zero ?
    // Datalength - the capacity of the Data ie) the maximum number of data pts that can be held in the object
    //boolean Y_Only = false;
    /**
     * Binning Data
     *
     */
    boolean binInY = true;
    double binWnd = 0;
    Iterator dataIterator;

    // ArrayList<Double[]> BinnedData;
    //public DataTrace( int length){
    // rawData = new ArrayList<>(length);
    /*if (length > 0){
            DataLength = length;
            xData = new double[DataLength];
            yData = new double[DataLength];
        }
        XData = new ArrayList<>();
        YData = new ArrayList<>();*/
    //}
    /**
     * default constructor
     *
     */
    public DataTrace_ver1() {
        //rawData = new ArrayList<OrdXYData>();
        //dataIterator = rawData.iterator();

    }

    /**
     * constructor defining length of data, bin width of x column
     *
     * @param <B> number object
     * @param datalength length of x data column in int
     * @param binWidth bin width of x data column
     * @param binInX set boolean for sort by x column to be true
     */
    public <B extends Number> DataTrace_ver1(int datalength, B binWidth, boolean binInX) {
        this.binWnd = binWidth.doubleValue();
        //rawData = new ArrayList(datalength);
        //dataIterator = rawData.iterator();
    }

    /**
     * add x,y data points
     *
     * @param <X> x column data points as number object
     * @param <Y> y column data points as number object
     * @param xData x data points as an array
     * @param yData y data points as an array
     */
    public <X extends Number, Y extends Number> void addData(X[] xData, Y[] yData) {
        //this.dataIterator = rawData.iterator();
        if (xData != null && yData != null && xData.length == yData.length) {
            int idx = 0;
            for (X x : xData) {
                addData(x, yData[idx++]); // It is  more efficient to add it directly to rawdata instead of calling addData.
                // But this ensures modularity. If any change to way we add elements we need to 
                // modify one function and in one place i.e addData()              
            }
        }
    }

    /**
     * add an individual x,y data point
     *
     * @param <X> x data point as number object
     * @param <Y> y data point as number object
     * @param xData x data point
     * @param yData y data point
     */
    public <X extends Number, Y extends Number> void addData(X xData, Y yData) {
        DataLength++;
        OrdXYData<X, Y> dataPt = new OrdXYData(DataLength, xData, yData);
        this.add(dataPt);

    }

    /*public ArrayList getNextXYData(){
      
     
     /*if (dataIterator.hasNext())
        return ((OrdXYData) dataIterator.next()).getXY();
     else 
         return null;*/
    //}
    /**
     * get data column length
     *
     * @return size of data column in int
     */
    public int getDataLength() {
        return this.size();
    }

    /**
     * get x column data points
     *
     * @param <N> Number object
     * @return x data points as an ArrayList
     */
    public <N extends Number> ArrayList getX() {
        ArrayList<N> x = new ArrayList();
        this.forEach((Data) -> {
            x.add((N) Data.getX());
        });
        return x;
    }

    /**
     * get y column data points
     *
     * @param <N> Number object
     * @return y data points as an ArrayList
     */
    public <N extends Number> ArrayList getY() {
        ArrayList<N> y = new ArrayList();
        this.forEach((Data) -> {
            y.add((N) Data.getY());
        });
        return y;
    }

    /**
     * reset statistics values on xy dataset
     *
     * @param <X> x data object
     * @param <Y> y data object
     */
    public <X extends Number, Y extends Number> void resetStat() {

        this.forEach((Data) -> {
            double x = ((X) Data.getX()).doubleValue();
            double y = ((Y) Data.getY()).doubleValue();

            x_Max = (x_Max > x) ? x_Max : x;
            y_Max = (y_Max > y) ? y_Max : y;

            x_Min = (x_Min < x) ? x_Min : x;
            y_Min = (y_Min < y) ? y_Min : y;

            x_Sum += x;
            y_Sum += y;
        });
    }

    /**
     * set or calculate statistics values on xy dataset
     *
     */
    private <X extends Number, Y extends Number> void setStat(X xData, Y yData) {

        double x = xData.doubleValue();
        double y = yData.doubleValue();

        x_Max = (x_Max > x) ? x_Max : x;
        y_Max = (y_Max > y) ? y_Max : y;

        x_Min = (x_Min < x) ? x_Min : x;
        y_Min = (y_Min < y) ? y_Min : y;

        x_Sum += x;
        y_Sum += y;
    }

    /**
     * get maximum value in x data column
     *
     * @return max x value as a double
     */
    public double getXMax() {
        return x_Max;
    }

    /**
     * get maximum value in y data column
     *
     * @return max y value as a double
     */
    public double getYMax() {
        return y_Max;
    }

    /**
     * get minimum value in x data column
     *
     * @return min x value as a double
     */
    public double getXMin() {
        return x_Min;
    }

    /**
     * get minimum value in y data column
     *
     * @return min y value as a double
     */
    public double getYMin() {
        return y_Min;
    }

    /**
     * get sum of x data points
     *
     * @return sum of x as double
     */
    public double getXSum() {
        return x_Sum;
    }

    /**
     * get sum of y data points
     *
     * @return sum of y as double
     */
    public double getYSum() {
        return y_Sum;
    }

    /**
     * get peak value of x data points
     *
     * @return peak x as double
     */
    public double getXPk() {
        return x_Max - x_Min;
    }

    /**
     * get peak value of y data points
     *
     * @return peak y as double
     */
    public double getYPk() {
        return y_Max - y_Min;
    }

    /**
     * clear x,y data columns
     *
     */
    public void resetTrace() {
        this.clear();
    }

    /**
     *
     * Differentiates the trace data and generates the differential of current
     * data will overwrite the current data with the differentiated data
     */
    public void differentiate() {
        differentiate(true);
    }

    /**
     * Differentiate the trace data and return the float array. if Overwrite is
     * true then the current data will be replaced by the differentiated data
     *
     * @param Overwrite
     * @return
     */
    public DataTrace_ver1 differentiate(boolean Overwrite) {
        DataTrace_ver1 difData = new DataTrace_ver1();
        //difData = null;
        return difData;
    }

    /**
     * bin data points
     *
     * @param <X>
     * @param <Y>
     * @param binWidth bin size
     * @param binInX bin wrt x column
     * @param restoreSeq maintain original data sequence
     * @return binned data points as dataTrace_ver1
     */
    public <X extends Number, Y extends Number> DataTrace_ver1 binData(double binWidth, boolean binInX, boolean restoreSeq) {

        DataTrace_ver1 binnedData = new DataTrace_ver1();
        this.sortData(binInX);
//        double binStart = (double) ((binInX) ? this.get(0).getX() : this.get(0).getY());
        double binStart = 0d;
        double binEnd = binStart + binWidth;
        double halfbinWidth = binWidth / 2;
        double binCtr = binStart + halfbinWidth;
//        double sum = (this.get(0).getY()).doubleValue();
        double sum = binStart; //modification by MP
        int count = 1;

        for (OrdXYData data : this) {

            double curX = ((double) data.getX());
            double curY = ((double) data.getY());

            //modified by MP - allows for binInX or binInY
            if (binInX) {
                //binInX
                if (curX >= binStart && curX < binEnd) {
                    sum += curY;
                    count++;
                } else {
                    double yData = sum / count;
                    binnedData.addData(binCtr, (sum / count));
                    sum = curY;
                    count = 1;
                    binStart = curX;
                    binCtr = binStart + halfbinWidth;
                    binEnd = binStart + binWidth;
                }
            } else if (curY >= binStart && curY < binEnd) {
                sum += curX;
                count++;
            } else {
                double xData = sum / count;
                binnedData.addData(binCtr, (sum / count));
                sum = curX;
                count = 1;
                binStart = curY;
                binCtr = binStart + halfbinWidth;
                binEnd = binStart + binWidth;
            }
        }
        return binnedData;
    }

    /**
     * sort data points in ascending order
     *
     * @param <X>
     * @param <Y>
     * @param XYData xy data points as dataTrace_ver1
     * @param inX sort wrt to x data column
     * @return sorted xy data as dataTrace_ver1
     */
    public <X extends Number, Y extends Number> DataTrace_ver1 sortXYData(DataTrace_ver1 XYData, boolean inX) {

        X[] x = (X[]) (XYData.getX().toArray());
        Y[] y = (Y[]) (XYData.getY().toArray());

        DataTrace_ver1 sortedData = new DataTrace_ver1();

        sortedData.addData(x, y);

        sortedData.sortData(inX);
        return sortedData;
    }

    private void sortData(boolean inX) {
        compareXofXYData xCmp = new compareXofXYData();
        compareYofXYData yCmp = new compareYofXYData();
        this.sort((inX) ? xCmp : yCmp);
    }

    private void resetOrder() {
        compareSerialofXYData cmp = new compareSerialofXYData();
        this.sort(cmp);
    }

    class compareXofXYData implements Comparator<OrdXYData> {

        @Override
        public int compare(OrdXYData t, OrdXYData t1) {
            return Double.compare(t.getX().doubleValue(), t1.getX().doubleValue());
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    class compareYofXYData implements Comparator<OrdXYData> {

        @Override
        public int compare(OrdXYData t, OrdXYData t1) {
            return Double.compare(t.getY().doubleValue(), t1.getY().doubleValue());
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    class compareSerialofXYData implements Comparator<OrdXYData> {

        @Override
        public int compare(OrdXYData t, OrdXYData t1) {
            return Integer.compare(t.getSerial(), t1.getSerial());
        }
    }

}
