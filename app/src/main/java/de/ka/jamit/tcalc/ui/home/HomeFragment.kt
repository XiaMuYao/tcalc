package de.ka.jamit.tcalc.ui.home


import android.os.Bundle
import de.ka.jamit.tcalc.R
import de.ka.jamit.tcalc.base.BaseFragment
import de.ka.jamit.tcalc.base.events.FragmentResultable
import de.ka.jamit.tcalc.databinding.FragmentHomeBinding
import de.ka.jamit.tcalc.ui.home.user.UserDialog
import timber.log.Timber

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(
        R.layout.fragment_home,
        HomeViewModel::class
), FragmentResultable {

    override fun getResultRequestKey(): String {
        return UserDialog.FRAGMENT_RESULT_KEY
    }

    override fun onFragmentResult(resultBundle: Bundle) {
        Timber.e("!!! result")
        viewModel.update()
    }
}
