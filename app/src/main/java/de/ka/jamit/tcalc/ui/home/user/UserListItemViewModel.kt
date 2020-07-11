package de.ka.jamit.tcalc.ui.home.user

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.jamit.tcalc.base.BaseItemViewModel
import de.ka.jamit.tcalc.repo.db.UserDao

/**
 * A ViewModel for a user list item.
 *
 * Created by Thomas Hofmann on 07.07.20
 **/
class UserListItemViewModel(val item: UserDao,
                            private val moreClickListener: (() -> Unit)? = null,
                            private val clickListener: ((UserListItemViewModel) -> Unit)? = null,
                            private val editListener: ((UserListItemViewModel) -> Unit)? = null,
                            private val deletionListener: (() -> Unit)? = null) :
        BaseItemViewModel() {

    val title = item.name

    val isDefaultItem = item.id == 1L

    val isMoreItem = moreClickListener != null

    val checkVisibility = if (item.selected) View.VISIBLE else View.INVISIBLE

    val editVisibility = if (isDefaultItem) View.GONE else View.VISIBLE

    fun onClick() {
        clickListener?.invoke(this)
        moreClickListener?.invoke()
    }

    fun onEdit() {
        editListener?.invoke(this)
    }

    fun onDismissed() {
        repository.deleteUser(item.id)
        deletionListener?.invoke()
    }
}