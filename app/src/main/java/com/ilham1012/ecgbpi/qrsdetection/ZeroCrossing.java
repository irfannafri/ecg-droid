package com.ilham1012.ecgbpi.qrsdetection;

import android.content.Intent;

import com.ilham1012.ecgbpi.qrsdetection.signalutils.FIRUtils;
import com.ilham1012.ecgbpi.qrsdetection.signalutils.FirFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hafid on 11/19/2017.
 */

public class ZeroCrossing {

    public double[] rawSignal;
    public int samplingRate;
    public double[] y1;
    public double[] y2;
    public double[] y3;
    public double[] y4;
    public ZeroCrossEvent y5;
    public SearchWindow y6;
    public double[] y7;
    public double[] thr;

    public ZeroCrossing(double[] x0, int nrate) {
        rawSignal = x0;
        samplingRate = nrate;
    }


    public void doZeroCrossing() {
//        TimingLogger timings = new TimingLogger("MainActivity", "ZeroCross");
        // Bandpass 18Hz - 35Hz
        y1 = bandpassFilter(rawSignal, 15, 45, 2);
//        timings.addSplit("Filtering");

        // Non-linear
        y2 = nonLinear(y1);
//        timings.addSplit("Non-linear");

        // High-freq Addition
        y3 = highFreqAdd(y2);
//        timings.addSplit("High Freq");

        // Zero-cross Count
        y4 = zeroCrossCount(y3);
//        timings.addSplit("Zero Cross Count");

        // Event detection
        y5 = eventDetection(y4, 0.75);
//        timings.addSplit("Event Detection");

        //Adaptive Threshold
        thr = adaptive_threshold(y4);
//        timings.addSplit("Adaptive Threshold");

        //Search Window
        y6 = search_window(y4, thr);
//        timings.addSplit("Search Windows");

        //Temporal Localization
        y7 = temporal_localization(y2, y6);
//        timings.addSplit("Temporal Localization");
//        timings.dumpToLog();

    }

    int fixorder = (int) Math.ceil(0.3 * samplingRate);

    public double[] bandpassFilter(double[] x, int low, int high, int order) {
        double[] y = new double[x.length];


        double[] filterCoef = FIRUtils.createBandpass(fixorder, low, high, samplingRate); //order
        FirFilter lowFirFilter = new FirFilter(filterCoef);

        for (int i = 0; i < x.length; i++) {
            y[i] = lowFirFilter.filter(x[i]);
        }

        return y;
    }


    public double[] nonLinear(double[] x) {
        double[] y = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            y[i] = signum(x[i]) * (x[i] * x[i]);
        }

        return y;
    }

    public int signum(double x) {
        int sign = 0;
        if (x > 0) {
            sign = 1;
        } else if (x < 0) {
            sign = -1;
        }

        return sign;
    }


    public double[] highFreqAdd(double[] x) {
        double kn, kn1, bn, forgettingFactor, c;
        double[] y = new double[x.length];

        kn1 = 0.0125;
        forgettingFactor = 1; // 0..1
        c = 3; // constant gain

        for (int i = 0; i < x.length; i++) {
//            kn = (i == 0) ? kn1 : forgettingFactor * kn1 + (1 - forgettingFactor) * Math.abs(x[i]) * c;
            kn = 0.0125;

//            if (i==0){
//                kn = kn1;
//            } else {
//                kn = forgettingFactor * kn1 + (1 - forgettingFactor) * Math.abs(x[i]) * c;
//            }

            if ((i & 1) == 0) {
                bn = kn;
            } else {
                bn = -1 * kn;
            }
//            bn = Math.pow(-1, i) * kn;

            y[i] = x[i] + bn;
            kn1 = kn;
        }

        return y;
    }


    public double[] zeroCrossCount(double[] x) {
        double[] y = new double[x.length];
        double[] dn = new double[x.length];
        double forgettingFactor = 0.8;


        for (int i = 0; i < x.length; i++) {
            if (i == 0) {
                dn[i] = 1; //Math.abs(Math.signum(x[i])/2);
                y[i] = 1; //forgettingFactor + (1 - forgettingFactor) * dn[i];
            } else {
                dn[i] = Math.abs((signum(x[i]) - signum(x[i - 1])) / 2);
                y[i] = forgettingFactor * y[i - 1] + (1 - forgettingFactor) * dn[i];
            }
        }

        return y;
    }

    public double[] adaptive_threshold(double[] x) {
        double[] y = new double[x.length];
        double forgettingFactor = 0.85;

        for (int i = 0; i < x.length; i++) {
            if (i == 0) {
                y[i] = 1; //forgettingFactor * (1 - forgettingFactor) * x[i];
            } else {
                y[i] = forgettingFactor * y[i - 1] + (1 - forgettingFactor) * x[i];
            }

        }

        return y;
    }

    public SearchWindow search_window(double[] x, double[] thr) {
        SearchWindow events = new SearchWindow();
        int last_start = -1;
        int last_end = -1;
        int last_status = 0;
        int cur_status = 0;
        int sum_start = 0;
        int sum_end = 0;
        float timelimit = (samplingRate * 12 / 100);

        for (int i = 0; i < x.length; i++) {
            if ((x[i]) < thr[i]) {
                cur_status = 1;
            } else {
                cur_status = 0;
            }

            if ((cur_status - last_status) == 1) {
                if (i > timelimit) {
                    if ((i - last_end) > timelimit) {
                        last_start = i;
//                        sum_start+=1;
                        if (i > 1) {
                            events.starts.add(i - 2);
                            ;
                        } else {
                            events.starts.add(i);
                        }
                    } else {
                        events.ends.remove(events.ends.size() - 1);
                    }
                } else {
//                  sum_start+=1;
                    last_start = i;
                    if (i > 1) {
                        events.starts.add(i - 2);
                        ;
                    } else {
                        events.starts.add(i);
                    }
                }
            } else if (cur_status - last_status == -1) {
                last_end = i;
//                sum_end +=1;
                events.ends.add(i);
            }

            last_status = cur_status;
//            if(sum_start>sum_end){
//                events.ends.add(x.length);
//            }
//
//            sum_start=0;
//            sum_end=0;
            /*if (y4[i] <= thr[i]){
                y6[i] = 0.5;}
            else
            {y6[i] = -0.5;}*/
        }

        if (events.starts.size() > events.ends.size()) {
            events.ends.add(x.length - 1);
        }

        return events;
    }

    public double[] temporal_localization(double[] x, SearchWindow events) {
        double[] y = new double[events.starts.size()];

        for (int i = 0; i < events.starts.size(); i++) {
            double maxpeak_val = 0;
            double maxpeak_loc = -1;
            double minpeak_val = 0;
            double minpeak_loc = -1;

            for (int j = events.starts.get(i); j < events.ends.get(i); j++) {
                if (x[j] > maxpeak_val) {
                    maxpeak_val = x[j];
                    maxpeak_loc = j;
                }

                if (x[j] < minpeak_val) {
                    minpeak_val = x[j];
                    minpeak_loc = j;
                }
            }

            if (maxpeak_val > Math.abs(minpeak_val)) {
                y[i] = maxpeak_loc;

            } else {
                y[i] = minpeak_loc;
            }
        }
        return y;
    }

    public ZeroCrossEvent eventDetection(double[] x, double treshold) {
        ZeroCrossEvent events = new ZeroCrossEvent();

        for (int i = 1; i < x.length; i++) {
            if ((x[i] <= treshold) && (x[i - 1] > treshold)) {
                if (events.ends.size() > 0) {
                    if (events.ends.get(events.ends.size() - 1) + 20 < i) {
                        events.starts.add(i);
                    } else {
                        events.ends.remove(events.ends.size() - 1);
                    }
                } else {
                    events.starts.add(i);
                }
            } else if ((x[i] >= treshold) && (x[i - 1] < treshold)) {
                events.ends.add(i);
            }
        }
        return events;
    }
}
