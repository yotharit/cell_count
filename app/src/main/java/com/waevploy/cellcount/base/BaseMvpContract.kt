package com.waevploy.cellcount.base

interface BaseMvpContract {
	interface View

	interface Presenter<V : View> {
		fun attachView(mvpView: V)
		fun detachView()
		fun getView(): V?
		fun onViewCreate()
		fun onViewDestroy()
		fun onViewStart()
		fun onViewStop()
	}
}
