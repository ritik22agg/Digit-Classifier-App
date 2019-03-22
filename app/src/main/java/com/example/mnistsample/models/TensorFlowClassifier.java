package com.example.mnistsample.models;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TensorFlowClassifier implements  Classifier{

    TensorFlowClassifier(){}

    public static float threshhold = 0.3f;


    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;

    public static Classifier create(AssetManager assetManager,
                             String modelPath,
                             String labelPath,
                             int inputSize
                             ) throws IOException {

        TensorFlowClassifier classifier = new TensorFlowClassifier();
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager,modelPath), new Interpreter.Options());
        classifier.labelList = classifier.loadLabelList(assetManager, labelPath);
        classifier.inputSize = inputSize;

        return classifier;
    }

    public List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String getModelPath(){
        return "model.tflite";
    }


    public float[] recognize(float[] pixels){
        float[][] ans = new float[1][10];
        interpreter.run(pixels,ans);
        float max = -1;
        float index = 0;
        for(int i =0;i<10;i++){
            if(max<ans[0][i]) {
                max = ans[0][i];
                index = i;
            }
        }
        float[] final1 = new float[2];
        final1[0] = index;
        final1[1] = max;
        return final1;
    }



}