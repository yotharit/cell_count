package com.waevploy.cellcount.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseMvpFragment<V : BaseMvpContract.View, P : BaseMvpContract.Presenter<V>> :
	Fragment(),
	BaseMvpContract.View {
	protected val mPresenter: P by lazy {
		createPresenter()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val layoutResId = getLayoutView()
		if (layoutResId == 0) {
			throw RuntimeException("No layout")
		}
		return inflater.inflate(layoutResId, container, false)
	}

	@Suppress("UNCHECKED_CAST")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mPresenter.attachView(this as V)
		arguments?.let {
			extractExtras(it)
		}
		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState)
		}
		bindView(view)
		setUpInstance()
		setUpView()
		mPresenter.onViewCreate()
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		if (savedInstanceState == null) {
			init()
		} else {
			restoreView(savedInstanceState)
		}
	}

	open fun onRestoreInstanceState(savedInstanceState: Bundle?) {

	}

	override fun onStart() {
		super.onStart()
		mPresenter.onViewStart()
	}

	override fun onStop() {
		super.onStop()
		mPresenter.onViewStop()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		mPresenter.onViewDestroy()
		mPresenter.detachView()
	}

	abstract fun createPresenter(): P
	abstract fun getLayoutView(): Int

	/**
	 * Extract extras from fragment's arguments
	 * called when fragment has arguments set.
	 */
	abstract fun extractExtras(arguments: Bundle)

	/**
	 * findViewById goes here.
	 */
	abstract fun bindView(v: View)

	/**
	 * Instantiate object (e.g., Adapter, Handler)
	 */
	abstract fun setUpInstance()

	/**
	 * Setup view related things (e.g., View.setOnClickListener, recyclerView setUp)
	 */
	abstract fun setUpView()

	/**
	 * Do things when fragment first created. (Load data)
	 */
	abstract fun init()

	/**
	 * Restore view's state from savedInstanceState.
	 * Called when configuration change.
	 */
	open fun restoreView(savedInstanceState: Bundle?) {

	}
}