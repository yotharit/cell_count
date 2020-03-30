package com.waevploy.cellcount.cell

import android.graphics.Bitmap
import android.net.Uri
import com.waevploy.cellcount.base.BaseMvpContract

class CellContract : BaseMvpContract {

	interface View : BaseMvpContract.View {
		fun alertOpenCv(cvAlert: Boolean)
		fun setResult(text: String)
	}

	interface Presenter : BaseMvpContract.Presenter<View> {
		fun findCellFromPlate(
			uri: Uri,
			brightness: Double,
			contrast: Double,
			minDistant: Double,
			minRadius: Int,
			maxRadius: Int
		): Bitmap?
	}
}