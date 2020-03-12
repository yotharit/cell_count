package com.waevploy.cellcount.cell

import android.graphics.Bitmap
import android.net.Uri
import com.waevploy.cellcount.base.BaseMvpContract

class CellContract : BaseMvpContract {

    interface View : BaseMvpContract.View {
        fun alertOpenCv(cvAlert: Boolean)
    }

    interface Presenter : BaseMvpContract.Presenter<View> {
        fun findCellFromPlate(uri: Uri) : Bitmap?
    }
}