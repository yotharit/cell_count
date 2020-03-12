package com.waevploy.cellcount.cell

import android.graphics.Bitmap
import android.net.Uri
import com.waevploy.cellcount.base.BaseMvpPresenter
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo


class CellPresenter : BaseMvpPresenter<CellContract.View>(), CellContract.Presenter {

    override fun onViewCreate() {
        if (OpenCVLoader.initDebug()) {
            getView()?.alertOpenCv(true)
        } else {
            getView()?.alertOpenCv(false)
        }
    }

    override fun findCellFromPlate(uri: Uri): Bitmap? {
        val maskedImage = maskCircle(uri)
        val addBrightness = addBrightness(maskedImage)
        val binaryImage = findBinarizeImage(addBrightness)
        val circular = calculateCircularHough(binaryImage)
        return circular
    }

    private fun maskCircle(uri: Uri): Bitmap {
        val src = Imgcodecs.imread(uri.path, CvType.CV_8U)
        val circle = Mat(src.height(), src.width(), CvType.CV_8U)
        val center = Point(((src.height() / 2).toDouble()), (src.width() / 2).toDouble())
        val radius = src.height() / 2
        Imgproc.circle(circle, center, radius, Scalar(255.0, 255.0, 255.0), -1)
        val dst = Mat(src.height(), src.width(), CvType.CV_8U)
        Imgproc.resize(circle, circle, src.size())
        Core.bitwise_and(src, circle, dst)
        val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)
        return bmp
    }

    private fun addBrightness(bitmap: Bitmap): Bitmap {
        val src = Mat(bitmap.height, bitmap.width, CvType.CV_8U)
        Utils.bitmapToMat(bitmap, src)
        Core.convertScaleAbs(src, src, 1.0, 50.0)
//        val increaseContrast = Mat(src.height(), src.width(), CvType.CV_8U)
//        Imgproc.equalizeHist(src, increaseContrast)
        val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(src, bmp)
        src.release()
        return bmp
    }

    private fun findBinarizeImage(bitmap: Bitmap): Bitmap {
        val src = Mat(bitmap.height, bitmap.width, CvType.CV_8U)
        Utils.bitmapToMat(bitmap, src)
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY)
        val dst = Mat(src.height(), src.width(), CvType.CV_8U)
        Imgproc.medianBlur(src, src, 3)
        val blurFilter = Mat(src.height(), src.width(), CvType.CV_8U)
        Imgproc.bilateralFilter(src, blurFilter, 11, 90.0, 90.0, Core.BORDER_ISOLATED)
//        Imgproc.threshold(src, dst, 127.0, 255.0, Imgproc.THRESH_BINARY)
//        Imgproc.adaptiveThreshold(
//            blurFilter, dst, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//            Imgproc.THRESH_BINARY, 7, 2.0
//        )
        Imgproc.adaptiveThreshold(
            blurFilter, dst, 500.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 21, 2.0
        )
        Core.bitwise_not(dst, dst)
        Photo.fastNlMeansDenoising(dst, dst)
        val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)
        src.release()
        dst.release()
        return bmp
    }

    private fun calculateCircularHough(bitmap: Bitmap): Bitmap? {
        val input = Mat(bitmap.height, bitmap.width, CvType.CV_8U)
        Utils.bitmapToMat(bitmap, input)
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        val circles = Mat()
        Imgproc.GaussianBlur(input, input, Size(9.0, 9.0), 2.0, 2.0);
        Imgproc.HoughCircles(
            input,
            circles,
            Imgproc.CV_HOUGH_GRADIENT,
            2.0,
            50.0,
            100.0,
            90.0,
            6,
            90
        )
        Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2RGB)
        for (x in 0 until circles.cols()) {
            val circleVec = circles.get(0, x) ?: break
            val center = Point(circleVec[0], circleVec[1])
            val radius = circleVec[2].toInt()
            Imgproc.circle(input, center, 3, Scalar(255.0, 0.0, 0.0), 10, 8, 0)
            Imgproc.circle(input, center, radius, Scalar(255.0, 0.0, 0.0), 5, 8, 0)
        }
        val bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(input, bmp)
        input.release()
        getView()?.setResult("Found : ${circles.cols()} Cells")
        return bmp
    }
}

