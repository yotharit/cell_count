package com.waevploy.cellcount.cell

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.waevploy.cellcount.R
import com.waevploy.cellcount.base.BaseMvpFragment
import kotlinx.android.synthetic.main.cell_fragment.*


class CellFragment : BaseMvpFragment<CellContract.View, CellContract.Presenter>(),
	CellContract.View {

	companion object {
		fun createInstance(): CellFragment {
			return CellFragment()
		}
	}

	override fun createPresenter(): CellContract.Presenter {
		return CellPresenter()
	}

	override fun getLayoutView(): Int = R.layout.cell_fragment

	override fun extractExtras(arguments: Bundle) {

	}

	override fun bindView(v: View) {

	}

	override fun setUpInstance() {

	}

	override fun setImage(bmp: Bitmap) {
		imageView.setImageBitmap(bmp)
	}

	override fun cropImage() {
		context?.let {
			CropImage.activity()
				.setActivityTitle("Cropping Plate")
				.setGuidelines(CropImageView.Guidelines.ON)
				.start(it, this)
		}
	}

	override fun cropMinRadius(cropImageUri: Uri) {
		context?.let {
			CropImage.activity(cropImageUri)
				.setActivityTitle("Cropping Smallest Cell")
				.setGuidelines(CropImageView.Guidelines.ON)
				.start(it, this)
		}
	}

	override fun cropMaxRadius(cropImageUri: Uri) {
		context?.let {
			CropImage.activity(cropImageUri)
				.setActivityTitle("Cropping Largest Cell")
				.setGuidelines(CropImageView.Guidelines.ON)
				.start(it, this)
		}
	}

	override fun setUpView() {
		chooseImageButton.setOnClickListener {
			val brightness = brightnessEditText.text.toString().toDouble()
			val contrast = contrastEditText.text.toString().toDouble()
			val param2 = param2EditText.text.toString().toDouble()
			mPresenter.setValue(brightness, contrast, param2)
			cropImage()
		}
		runAgainImageButton.setOnClickListener {
			val brightness = brightnessEditText.text.toString().toDouble()
			val contrast = contrastEditText.text.toString().toDouble()
			val param2 = param2EditText.text.toString().toDouble()
			mPresenter.setValue(brightness, contrast, param2)
			mPresenter.reprocess()
		}
	}

	override fun init() {

	}

	override fun alertOpenCv(cvAlert: Boolean) {
		if (cvAlert) {
			Toast.makeText(context, "openCv successfully loaded", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "openCv cannot be loaded", Toast.LENGTH_LONG).show();
		}
	}

	override fun setResult(text: String) {
		resultTextView.text = text
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			val result = CropImage.getActivityResult(data)
			if (resultCode == Activity.RESULT_OK) {
				mPresenter.handleLogic(result.uri)
			} else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
				val error = result.error
				Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}

}