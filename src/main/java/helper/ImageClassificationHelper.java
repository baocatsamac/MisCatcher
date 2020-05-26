package helper;

import org.apache.commons.io.FilenameUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.CpuBackend;
import org.nd4j.linalg.io.ClassPathResource;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import service.Holder;

import java.io.IOException;
import java.util.ArrayList;

public class ImageClassificationHelper {

    private static String[] labels = {"location-point", "home", "video", "contact", "calendar", "microphone", "sms", "heart", "storage-directory", "arrow", "mail", "camera", "recording", "gallery", "share-up", "trash", "storage-download-upload", "search", "star", "phone", "setting", "play", "storage-disk", "share-network", "arrow-1", "plus", "location-marker"};
    private static double[] thresholds = {0.99, 0.9, 0.99, 0.95, 0.9995, 0.999, 0.99, 0.9, 0.99, 0.95, 0.9, 0.99, 0.9, 0.99, 0.9, 0.9, 0.99, 0.9, 0.99, 0.993, 0.9, 0.9, 0.99, 0.9, 0.9, 0.9, 0.99};

    public static String classifyImage(String imagePath) {
        String classifiedLabel = "unknown";
        try {

            // OpenCV loading
            // For proper execution of native libraries
            // Core.NATIVE_LIBRARY_NAME must be loaded before
            // calling any of the opencv methods
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            // set CpuBackend class loader from deeplearning4j as class loader of the plugin
            Thread.currentThread().setContextClassLoader(CpuBackend.class.getClassLoader());


            // input image
            imagePath = FilenameUtils.separatorsToSystem(imagePath);
            Mat imageMat = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR);
            NativeImageLoader nativeImageLoader = new NativeImageLoader();

            // resize image to 128x128
            Mat dstMat = new Mat();
            Imgproc.resize(imageMat, dstMat, new Size(128, 128));
            INDArray imageArray = nativeImageLoader.asMatrix(dstMat);
            // convert pixel value from [0:255] to [0:1]
            imageArray.divi(255);
            INDArray imageINDArray = Holder.getImageClassificationModel().output(imageArray);
            int index = Integer.parseInt(imageINDArray.getRow(0).argMax().toString());

            if (imageINDArray.getDouble(0, index) >= thresholds[index]) {
                classifiedLabel = labels[index];
            }
            System.out.println("\n Classified label is as " + classifiedLabel);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return classifiedLabel;
    }

    public static void loadModel() {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            // set CpuBackend class loader from deeplearning4j as class loader of the plugin
            Thread.currentThread().setContextClassLoader(CpuBackend.class.getClassLoader());

            // load trained image classification model
            String simpleMlp = new ClassPathResource("final_resources_model.h5").getFile().getPath();
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
            Holder.setImageClassificationModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKerasConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedKerasConfigurationException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

}
