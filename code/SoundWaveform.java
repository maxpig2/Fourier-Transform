

// DO NOT DISTRIBUTE THIS FILE TO STUDENTS
import ecs100.UI;

import java.awt.*;
import java.util.ArrayList;

/*
  getAudioInputStream
  -> getframelength,
  -> read into byteArray of 2x that many bytes
  -> convert to array of doubles in reversed pairs of bytes (signed)
  -> scale #FFFF to +/- 300

  array of doubles
   -> unscale  +/- 300  to #FFFF (
   -> convert to array of bytes (pairs little endian, signed)
   -> convert to inputStream
   -> convert to AudioInputStream
   -> write to file.
 */



public class SoundWaveform{
    public static final double MAX_VALUE = 300;
    public static final int SAMPLE_RATE = 44100;
    public static final int MAX_SAMPLES = SAMPLE_RATE/100;   // samples in 1/100 sec

    public static final int GRAPH_LEFT = 10;
    public static final int ZERO_LINE = 310;
    public static final int X_STEP = 2;            //pixels between samples
    public static final int GRAPH_WIDTH = MAX_SAMPLES*X_STEP;

    private ArrayList<Double> waveform = new ArrayList<Double>();   // the displayed waveform
    private ArrayList<ComplexNumber> spectrum = new ArrayList<ComplexNumber>(); // the spectrum: length/mod of each X(k)
    /**
     * Displays the waveform.
     */
    public void displayWaveform(){
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<this.waveform.size(); i++){
            double y1 = ZERO_LINE - this.waveform.get(i-1);
            double y2 = ZERO_LINE - this.waveform.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    /**
     * Displays the spectrum. Scale to the range of +/- 300.
     */
    public void displaySpectrum() {
        if (this.spectrum == null){ //there is no data to display
            UI.println("No spectrum to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // calculate the mode of each element
        ArrayList<Double> spectrumMod = new ArrayList<Double>();
        double max = 0;
        for (int i = 0; i < spectrum.size(); i++) {
            if (i == MAX_SAMPLES)
                break;

            double value = spectrum.get(i).mod();
            max = Math.max(max, value);
            spectrumMod.add(spectrum.get(i).mod());
        }

        double scaling = 300/max;
        for (int i = 0; i < spectrumMod.size(); i++) {
            spectrumMod.set(i, spectrumMod.get(i)*scaling);
        }

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<spectrumMod.size(); i++){
            double y1 = ZERO_LINE;
            double y2 = ZERO_LINE - spectrumMod.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    public void dft() {
        UI.clearText();
        UI.println("DFT in process, please wait...");

        // TODO
        // Add your code here: you should transform from the waveform to the spectrum

        /*
        ArrayList<ComplexNumber> waveformConverted = new ArrayList();
        for (double d: waveform) {
            waveformConverted.add(new ComplexNumber(d,0));
        }
        */

        spectrum.clear();
        double N = waveform.size();
        for (int k = 0; k < N; k++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int n = 0; n < N; n++) {
                ComplexNumber tmp = new ComplexNumber(waveform.get(n),0);
                tmp.multiply(new ComplexNumber(Math.cos(-n*k*(2* Math.PI)/N), Math.sin(-n*k*(2* Math.PI)/N)));
                sum.add(tmp);
            }
            spectrum.add(sum);
        }
        UI.println("DFT completed!");
        waveform.clear();
        displaySpectrum();
    }

    public void idft() {
        UI.clearText();
        UI.println("IDFT in process, please wait...");

        // TODO
        // Add your code here: you should transform from the spectrum to the waveform
        waveform.clear();

        double N = spectrum.size();
        for (int k = 0; k < N; k++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int n = 0; n < N; n++) {
                ComplexNumber tmp = spectrum.get(n).copy();
                tmp.multiply(new ComplexNumber(Math.cos(n*k*(2* Math.PI)/N), Math.sin(n*k*(2* Math.PI)/N)));
                sum.add(tmp);
            }
            waveform.add(sum.getRe()/N);
        }

        UI.println("IDFT completed!");

        spectrum.clear();
        displayWaveform();

    }



    public ComplexNumber W (int k, int N) {
        return (new ComplexNumber(Math.cos(-k*(2* Math.PI)/N), Math.sin(-k*(2* Math.PI)/N)));
    }

    public ComplexNumber[] rFFT(ComplexNumber[] x, int size) {
        if (size == 1) {
            return x;
        }
        ComplexNumber[] X = new ComplexNumber[size];
        ComplexNumber[] xeven = new ComplexNumber[size / 2];
        ComplexNumber[] xodd = new ComplexNumber[size / 2];

        for (int i = 0; i < size/2; i++) {
            xeven[i] = x[i * 2];
            xodd[i] = x[i * 2 + 1];
        }

        xeven = rFFT(xeven, size / 2);
        xodd = rFFT(xodd, size / 2);

        for (int k = 0; k < size / 2; k++) {
           ComplexNumber tmp = W(k,size);
           tmp.multiply(xodd[k]);
           tmp.add(xeven[k]);
           X[k] = tmp;

           tmp =  W(k + (size/2),size);
           tmp.multiply(xodd[k]);
           tmp.add(xeven[k]);
           X[k + (size/2)] = tmp;
        }

        return X;
    }

    public void fft() {
        UI.clearText();
        UI.println("FFT in process, please wait...");

        // TODO
        // Add your code here: you should transform from the waveform to the spectrum
        spectrum.clear();

        int N = 1;
        while (N <= waveform.size()) {
            N = N*2;
        }
        N=N/2;

        ComplexNumber[] x = new ComplexNumber[N];
        for (int i =0; i < N; i++) {
            x[i] = new ComplexNumber(waveform.get(i),0);
        }

        ComplexNumber[] X = rFFT(x,N);

        for (int j =0; j < N; j++) {
            spectrum.add(X[j]);
        }


        UI.println("FFT completed!");
        waveform.clear();
        displaySpectrum();
    }











    public ComplexNumber IW (int k, int N) {
        return (new ComplexNumber(Math.cos(k*(2* Math.PI)/N), Math.sin(k*(2* Math.PI)/N)));
    }


    public ComplexNumber[] rIFFT(ComplexNumber[] x, int size) {
        if (size == 1) {
            return x;
        }
        ComplexNumber[] X = new ComplexNumber[size];
        ComplexNumber[] xeven = new ComplexNumber[size / 2];
        ComplexNumber[] xodd = new ComplexNumber[size / 2];

        for (int i = 0; i < size/2; i++) {
            xeven[i] = x[i * 2];
            xodd[i] = x[i * 2 + 1];
        }

        xeven = rIFFT(xeven, size / 2);
        xodd = rIFFT(xodd, size / 2);

        for (int k = 0; k < size / 2; k++) {
            ComplexNumber tmp = IW(k,size);
            tmp.multiply(xodd[k]);
            tmp.add(xeven[k]);
            X[k] = tmp;

            tmp =  IW(k + (size/2),size);
            tmp.multiply(xodd[k]);
            tmp.add(xeven[k]);
            X[k + (size/2)] = tmp;
        }

        return X;
    }

    public void ifft() {
        UI.clearText();
        UI.println("IFFT in process, please wait...");

        // TODO
        // Add your code here: you should transform from the spectrum to the waveform

        // TODO
        // Add your code here: you should transform from the waveform to the spectrum
        waveform.clear();

        int N = 1;
        while (N <= spectrum.size()) {
            N = N*2;
        }
        N=N/2;

        ComplexNumber[] x = new ComplexNumber[N];
        for (int i =0; i < N; i++) {
            x[i] = spectrum.get(i).copy();
        }

        ComplexNumber[] X = rIFFT(x,N);

        for (int j =0; j < N; j++) {
            waveform.add((X[j].getRe()/N));
        }


        displayWaveform();

        UI.println("IFFT completed!");

        spectrum.clear();
    }

    /**
     * Save the wave form to a WAV file
     */
    public void doSave() {
        WaveformLoader.doSave(waveform, WaveformLoader.scalingForSavingFile);
    }

    /**
     * Load the WAV file.
     */
    public void doLoad() {
        UI.clearText();
        UI.println("Loading...");

        waveform = WaveformLoader.doLoad();

        this.displayWaveform();

        UI.println("Loading completed!");
    }

    public static void main(String[] args){
        SoundWaveform wfm = new SoundWaveform();
        //core
        UI.addButton("Display Waveform", wfm::displayWaveform);
        UI.addButton("Display Spectrum", wfm::displaySpectrum);
        UI.addButton("DFT", wfm::dft);
        UI.addButton("IDFT", wfm::idft);
        UI.addButton("FFT", wfm::fft);
        UI.addButton("IFFT", wfm::ifft);
        UI.addButton("Save", wfm::doSave);
        UI.addButton("Load", wfm::doLoad);
        UI.addButton("Quit", UI::quit);
      //  UI.setMouseMotionListener(wfm::doMouse);
        UI.setWindowSize(950, 630);
    }
}
