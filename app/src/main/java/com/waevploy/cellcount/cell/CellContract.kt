package com.waevploy.cellcount.cell

import android.graphics.Bitmap
import android.net.Uri
import com.waevploy.cellcount.base.BaseMvpContract

class CellContract : BaseMvpContract {

	interface View : BaseMvpContract.View {
		fun alertOpenCv(cvAlert: Boolean)
		fun setResult(text: String)
		fun cropImage()
		fun cropMinRadius(cropImageUri: Uri)
		fun cropMaxRadius(cropImageUri: Uri)
		fun setImage(bmp: Bitmap)
	}

	interface Presenter : BaseMvpContract.Presenter<View> {
		fun handleLogic(uri: Uri)
		fun setValue(
			brightness: Double,
			contrast: Double,
			param2: Double
		)
	}
}