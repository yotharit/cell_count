package com.waevploy.cellcount.cell

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.waevploy.cellcount.base.BaseMvpPresenter
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.min


class CellPresenter : BaseMvpPresenter<CellContract.View>(), CellContract.Presenter {
    override fun onViewCreate() {
        if (OpenCVLoader.initDebug()) {
            getView()?.alertOpenCv(true)
        } else {
            getView()?.alertOpenCv(false)
        }
    }

    override fun findCellFromPlate(uri: Uri): Bitmap? {
        val binarized = findBinarizeImage(uri)
        return calculateCircularHough(binarized)
    }

    private fun findBinarizeImage(uri: Uri): Bitmap {
        val src = Imgcodecs.imread(uri.path)
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY)
        val dst = Mat(src.height(), src.width(), CvType.CV_8U)
//        Imgproc.medianBlur(src,src,3)
//        Imgproc.GaussianBlur(src,src,Size(5.0,5.0),0.0)
//        Imgproc.threshold(src, dst, 120.0, 255.0, Imgproc.THRESH_BINARY)
        // Bilateral Filter to smooth the image
        val blurFilter = Mat(src.height(), src.width(), CvType.CV_8U)
        Imgproc.bilateralFilter(src, blurFilter, 9, 75.0, 75.0, Core.BORDER_DEFAULT)
//        Imgproc.adaptiveThreshold(
//            blurFilter, dst, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//            Imgproc.THRESH_BINARY, 7, 2.0
//        )
        Imgproc.adaptiveThreshold(
            blurFilter, dst, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY, 7, 2.0
        )
        Core.bitwise_not(dst, dst)
        val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)
        src.release()
        dst.release()
        return bmp
    }

    private fun calculateCircularHough(bitmap: Bitmap): Bitmap? {
//        val input = Imgcodecs.imread(uri.path, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE)
        Log.d("LogCircularHough","input")
        val input = Mat(bitmap.height,bitmap.width,CvType.CV_8U)
        Log.d("LogCircularHough","map to mat")
        Utils.bitmapToMat(bitmap,input)
        Log.d("LogCircularHough","convert to gray")
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        val circles = Mat()
        Log.d("LogCircularHough","blur")
        Imgproc.blur(input, input, Size(7.0, 7.0), Point(2.0, 2.0))
        Log.d("LogCircularHough","Hough")
        Imgproc.HoughCircles(
            input,
            circles,
            Imgproc.CV_HOUGH_GRADIENT,
            2.0,
            100.0,
            100.0,
            90.0,
            10,
            50
        )
        Log.d("LogCircularHough","draw circle")
        for (x in 0 until circles.cols()) {
            val circleVec = circles.get(0, x) ?: break
            val center = Point(circleVec[0], circleVec[1])
            val radius = circleVec[2].toInt()

            Imgproc.circle(input, center, 3, Scalar(255.0, 255.0, 255.0), 5)
            Imgproc.circle(input, center, radius, Scalar(255.0, 255.0, 255.0), 2)
        }
        val bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(input, bmp)
        input.release()
        return bmp
    }
}

