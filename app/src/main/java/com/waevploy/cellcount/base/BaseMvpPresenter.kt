package com.waevploy.cellcount.base

import java.lang.ref.WeakReference

abstract class BaseMvpPresenter<V : BaseMvpContract.View> : BaseMvpContract.Presenter<V> {

	private var mMvpView: WeakReference<V>? = null

	override fun attachView(mvpView: V) {
		mMvpView = WeakReference(mvpView)
	}

	override fun detachView() {
		mMvpView = null
	}

	override fun getView(): V? {
		mMvpView?.get()
			?.let {
				return it
			}
		return null
	}

	override fun onViewCreate() {

	}

	override fun onViewDestroy() {

	}

	override fun onViewStart() {

	}

	override fun onViewStop() {

	}
}
