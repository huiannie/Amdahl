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

package org.csc205.template.amdahl.ui;

import android.app.Fragment;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.csc205.template.amdahl.R;
import org.csc205.template.amdahl.app.AppSettings;
import org.csc205.template.amdahl.engine.Amdahl;
import org.csc205.template.amdahl.io.Savelog;


public class GraphFragment extends Fragment {
    private static final String TAG = GraphFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_typeCode = GraphFragment.class.getSimpleName()+".typeCode";
    private static final String defaultTypeCode = Amdahl.type_default;

    private static final int Input_graphType = 0;
    private static final int Input_curveCount = 1;
    private static final int Input_maxK = 2;

    private static final int[] colors = Graph.standardColors; // use colors provided by the Graph
    private static final int maxCurves = Graph.standardColors.length;

    private static final Double[] maxKs = {Double.valueOf(2), Double.valueOf(5), Double.valueOf(10), Double.valueOf(100), Double.valueOf(1000)};


    private String YLabel = "";
    private String XLabel = "";

    private String mTypeCode;
    private Amdahl mAmdahl;
    private int curveCount = maxCurves;
    private static final int pointCount = 60;

    double maxK = maxKs[0];

    private double Xmin;
    private double Xmax;
    private double Ymin;
    private double Ymax;

    private PointF[][] mCurves;
    private String[] mCurveLabels;

    private Graph mGraph;
    private Graph.VerticalBarMarker mMarker;

    private OnGraphValueChangedListener mOnGraphValueChangedListener;

    private String[] AmdahlPlotTypes = Amdahl.plotTypes;
    private Spinner mTypeSpinner = null;
    private ArrayAdapter<String> mTypeAdapter = null;
    private SpinnerOnItemSelectedListener mTypeSpinnerListener;

    private Spinner mCountSpinner = null;
    private ArrayAdapter<Integer> mCountAdapter = null;
    private SpinnerOnItemSelectedListener mCountSpinnerListener;

    private Spinner mMaxKSpinner = null;
    private ArrayAdapter<Double> mMaxKAdapter = null;
    private SpinnerOnItemSelectedListener mMaxKSpinnerListener;


    public static GraphFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("")) typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        GraphFragment fragment = new GraphFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);


        mAmdahl = new Amdahl(defaultTypeCode, maxK);
        initiateGraphParameters();

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_graph, parent, false);


        // Allow reuse
        if (mTypeAdapter ==null)
            mTypeAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, AmdahlPlotTypes);
        // Allow reuse
        if (mTypeSpinnerListener ==null)
            mTypeSpinnerListener = new SpinnerOnItemSelectedListener(this, Input_graphType);

        mTypeSpinner = (Spinner) v.findViewById(R.id.fragmentGraph_graphType);
        mTypeSpinner.setAdapter(mTypeAdapter);
        mTypeSpinner.setOnItemSelectedListener(mTypeSpinnerListener);
        selectSpinnerItem(Input_graphType);


        // Allow reuse
        if (mCountAdapter==null) {
            Integer counts[] = new Integer[maxCurves];
            for (int i=0; i<maxCurves; i++) counts[i] = i+1;
            mCountAdapter = new ArrayAdapter<Integer>(getActivity().getApplicationContext(), R.layout.spinner_item, counts);
        }
        // Allow reuse
        if (mCountSpinnerListener==null) {
            mCountSpinnerListener = new SpinnerOnItemSelectedListener(this, Input_curveCount);
        }
        mCountSpinner = (Spinner) v.findViewById(R.id.fragmentGraph_curveCount);
        mCountSpinner.setAdapter(mCountAdapter);
        mCountSpinner.setOnItemSelectedListener(mCountSpinnerListener);
        selectSpinnerItem(Input_curveCount);


        // Allow reuse
        if (mMaxKAdapter==null) {
            mMaxKAdapter = new ArrayAdapter<Double>(getActivity().getApplicationContext(), R.layout.spinner_item, maxKs);
        }
        // Allow reuse
        if (mMaxKSpinnerListener==null) {
            mMaxKSpinnerListener = new SpinnerOnItemSelectedListener(this, Input_maxK);
        }
        mMaxKSpinner = (Spinner) v.findViewById(R.id.fragmentGraph_maxK);
        mMaxKSpinner.setAdapter(mMaxKAdapter);
        mMaxKSpinner.setOnItemSelectedListener(mMaxKSpinnerListener);
        selectSpinnerItem(Input_maxK);



        mGraph = (Graph) v.findViewById(R.id.fragmentGraph_graph);

        mGraph.setCurves(Xmin, Xmax, Ymin, Ymax, mCurves);
        mGraph.setCurveLabels(mCurveLabels);
        mGraph.setAxesLabels(XLabel, YLabel);
        mGraph.setCurveColors(colors);

        // Allow reuse
        if (mOnGraphValueChangedListener==null)
            mOnGraphValueChangedListener = new OnGraphValueChangedListener(this);

        mGraph.setOnVerticalBarChangeListener(mOnGraphValueChangedListener);
        mGraph.setNotifyWhileDragging(true);
        mGraph.setVerticalBarMarker(mMarker);

        return v;
    }



    @Override
    public void onDestroyView() {
        if (mGraph!=null) {
            mGraph.cleanup();
            mGraph = null;
        }
        if (mTypeSpinner !=null) {
            mTypeSpinner.setAdapter(null);
            mTypeSpinner.setOnItemSelectedListener(null);
            mTypeSpinner = null;
        }
        if (mCountSpinner !=null) {
            mCountSpinner.setAdapter(null);
            mCountSpinner.setOnItemSelectedListener(null);
            mCountSpinner = null;
        }
        if (mMaxKSpinner !=null) {
            mMaxKSpinner.setAdapter(null);
            mMaxKSpinner.setOnItemSelectedListener(null);
            mMaxKSpinner = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mAmdahl = null;
        mMarker = null;
        mCurveLabels = null;
        mCurves = null;

        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        if (mOnGraphValueChangedListener!=null) {
            mOnGraphValueChangedListener.cleanup();
            mOnGraphValueChangedListener = null;
        }

        if (mTypeSpinnerListener !=null) {
            mTypeSpinnerListener.cleanup();
            mTypeSpinnerListener = null;
        }
        mTypeAdapter = null;

        if (mCountSpinnerListener !=null) {
            mCountSpinnerListener.cleanup();
            mCountSpinnerListener = null;
        }
        mCountAdapter = null;

        if (mMaxKSpinnerListener !=null) {
            mMaxKSpinnerListener.cleanup();
            mMaxKSpinnerListener = null;
        }
        mMaxKAdapter = null;

        super.onDestroy();
    }


    private void selectSpinnerItem(int type) {
        if (type==Input_graphType) {
            for (int index = 0; index < AmdahlPlotTypes.length; index++) {
                if (AmdahlPlotTypes[index].equals(mTypeCode)) {
                    if (mTypeSpinner != null) {
                        mTypeSpinner.setSelection(index);
                    }
                    return;
                }
            }
        }
        else if (type== Input_curveCount) {
            for (int index = 0; index < maxCurves; index++) {
                if (index+1== curveCount) {
                    if (mCountSpinner != null) {
                        mCountSpinner.setSelection(index);
                    }
                    return;
                }
            }
        }
        else if (type==Input_maxK) {
            for (int index = 0; index < maxKs.length; index++) {
                if (maxKs[index]==maxK) {
                    if (mMaxKSpinner != null) {
                        mMaxKSpinner.setSelection(index);
                    }
                    return;
                }
            }

        }
    }



    private void initiateGraphParameters() {
        if (mMarker==null) {
            mMarker = new Graph.VerticalBarMarker();
        }

        mAmdahl.setType(mTypeCode);
        mAmdahl.setMaxK(maxK);

        Xmin = mAmdahl.getMinX();
        Xmax = mAmdahl.getMaxX();
        Ymin = mAmdahl.getMinY();
        Ymax = mAmdahl.getMaxY();


        XLabel = mAmdahl.getXType();
        YLabel = mAmdahl.getYType();
        mCurves = mAmdahl.getPoints(curveCount, pointCount);

        String curveFixedParamLabel = mAmdahl.getCurveFixedParamLabel();
        double[] curveFixedParams = mAmdahl.getCurveFixedParams(curveCount);
        mCurveLabels = new String[curveCount];
        for (int i=0; i<curveCount; i++) {
            mCurveLabels[i] = curveFixedParamLabel + "=" + String.format("%.2f", curveFixedParams[i]);
        }

        mMarker.FX = (Xmin+Xmax)*0.5;
        mMarker.FYs = new double[curveCount];
        computeYvalues(mMarker.FX);

        // If the graph view already exists, then update it.
        if (mGraph!=null) {
            mGraph.setCurves(Xmin, Xmax, Ymin, Ymax, mCurves);
            mGraph.setCurveLabels(mCurveLabels);
            mGraph.setAxesLabels(XLabel, YLabel);
            mGraph.setCurveColors(colors);
            mGraph.setVerticalBarMarker(mMarker);
            mGraph.setThumbPositionByMarker();
            mGraph.invalidate();
        }
    }




    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        GraphFragment hostFragment;
        int type;
        public SpinnerOnItemSelectedListener(GraphFragment hostFragment, int type) {
            this.hostFragment = hostFragment;
            this.type = type;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (type==Input_graphType) {
                if (!hostFragment.mTypeCode.equals(hostFragment.AmdahlPlotTypes[pos])) {
                    hostFragment.mTypeCode = hostFragment.AmdahlPlotTypes[pos];
                    // redraw graph
                    hostFragment.initiateGraphParameters();
                }
            }
            else if (type== Input_curveCount) {
                if (hostFragment.curveCount !=(pos+1)) {
                    hostFragment.curveCount = pos+1;
                    hostFragment.initiateGraphParameters();
                }
            }
            else if (type==Input_maxK) {
                if (hostFragment.maxK!=maxKs[pos]) {
                    hostFragment.maxK = maxKs[pos];
                    hostFragment.initiateGraphParameters();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void cleanup() {
            hostFragment = null;
        }
    }



    private static class OnGraphValueChangedListener implements Graph.OnVerticalBarChangeListener{
        // This class of objects does not outlive its host, so no need to use weak references
        GraphFragment hostFragment;
        public OnGraphValueChangedListener(GraphFragment hostFragment) {
            this.hostFragment = hostFragment;
        }
        @Override
        public void onVerticalBarValuesChanged(Graph graph, double x) {
            hostFragment.computeYvalues(x);
            graph.setVerticalBarMarker(hostFragment.mMarker);
        }
        public void cleanup() { hostFragment = null; }
    }


    public void computeYvalues(double x) {
        mMarker.FX = x;
        Savelog.d(TAG, debug, "setting marker.x="+mMarker.FX);
        mMarker.FYs = mAmdahl.getYs(x, curveCount);
    }
}
