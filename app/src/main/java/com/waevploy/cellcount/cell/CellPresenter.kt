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

	override fun findCellFromPlate(
		uri: Uri,
		brightness: Double,
		contrast: Double,
		minDistant: Double,
		minRadius: Int,
		maxRadius: Int
	): Bitmap? {
		val adjustedImage = adjustImage(uri, brightness, contrast)
		val maskedImage = maskCircle(adjustedImage)
		val addBrightness = addBrightness(maskedImage)
		val binaryImage = findBinarizeImage(addBrightness)
		return calculateCircularHough(
			binaryImage,
			minDistant,
			minRadius,
			maxRadius
		)
	}

	private fun adjustImage(uri: Uri, brightness: Double, contrast: Double): Bitmap {
		var src = Imgcodecs.imread(uri.path)
		Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB)
		adjustBrightnessAndContrast(src, brightness, contrast)
		Photo.fastNlMeansDenoisingColored(src, src)
		val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(src, bmp)
		src.release()
		return bmp
	}

	private fun adjustBrightnessAndContrast(src: Mat, brightness: Double, contrast: Double) {
		var shadow = 0.0
		var highlight = 0.0
		var alphaB = 0.0
		var gammaB = 0.0
		if (brightness != 0.0) {
			if (brightness > 0) {
				shadow = brightness
				highlight = 255.0
			} else {
				shadow = 0.0
				highlight = 255 + brightness
			}
			alphaB = (highlight - shadow) / 255
			gammaB = shadow
			Core.addWeighted(src, alphaB, src, 0.0, gammaB, src)
		}
		var alphaC = 0.0
		var gammaC = 0.0
		var f = 0.0
		if (contrast != 0.0) {
			f = 131 * (contrast + 127) / (127 * (131 - contrast))
			alphaC = f
			gammaC = 127 * (1 - f)
			Core.addWeighted(src, alphaC, src, 0.0, gammaC, src)
		}
	}

	private fun maskCircle(bitmap: Bitmap): Bitmap {
		val src = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
		Utils.bitmapToMat(bitmap, src)
		val circle = Mat(src.height(), src.width(), CvType.CV_8UC4)
		val center = Point(((src.height() / 2).toDouble()), (src.width() / 2).toDouble())
		val radius = src.height() / 2
		Imgproc.circle(circle, center, radius, Scalar(255.0, 255.0, 255.0), -1)
		val dst = Mat(src.height(), src.width(), CvType.CV_8UC4)
		Imgproc.resize(circle, circle, src.size())
		Core.bitwise_and(src, circle, dst)
		Photo.fastNlMeansDenoisingColored(dst, dst)
		val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(dst, bmp)
		return bmp
	}

	private fun addBrightness(bitmap: Bitmap): Bitmap {
		val src = Mat(bitmap.height, bitmap.width, CvType.CV_8U)
		Utils.bitmapToMat(bitmap, src)
		Core.convertScaleAbs(src, src, 1.0, 10.0)
		val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(src, bmp)
		src.release()
		return bmp
	}

	private fun findBinarizeImage(
		bitmap: Bitmap
	): Bitmap {
		val src = Mat(bitmap.height, bitmap.width, CvType.CV_8U)
		Utils.bitmapToMat(bitmap, src)
		Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY)
		val dst = Mat(src.height(), src.width(), CvType.CV_8U)
		Imgproc.medianBlur(src, src, 3)
		val blurFilter = Mat(src.height(), src.width(), CvType.CV_8U)
		Imgproc.bilateralFilter(src, blurFilter, 11, 90.0, 90.0, Core.BORDER_ISOLATED)
		Imgproc.adaptiveThreshold(
			blurFilter, dst, 500.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
			Imgproc.THRESH_BINARY, 21, 2.0
		)
		Core.bitwise_not(dst, dst)
		for (i in 0..2) {
			Photo.fastNlMeansDenoising(dst, dst)
		}
		val bmp = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(dst, bmp)
		src.release()
		dst.release()
		return bmp
	}

	private fun calculateCircularHough(
		bitmap: Bitmap,
		minDistant: Double,
		minRadius: Int,
		maxRadius: Int
	): Bitmap? {
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
			minDistant,
			90.0,
			100.0,
			minRadius,
			maxRadius
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

