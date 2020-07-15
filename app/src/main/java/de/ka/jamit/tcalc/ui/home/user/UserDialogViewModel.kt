package de.ka.jamit.tcalc.ui.home.user

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.jamit.tcalc.R
import de.ka.jamit.tcalc.base.BaseViewModel
import de.ka.jamit.tcalc.base.events.ShowSnack
import de.ka.jamit.tcalc.repo.db.UserDao
import de.ka.jamit.tcalc.ui.home.addedit.HomeAddEditDialog
import de.ka.jamit.tcalc.ui.home.user.addedit.UserAddEditDialog
import de.ka.jamit.tcalc.utils.Snacker
import de.ka.jamit.tcalc.utils.resources.ResourcesProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import org.koin.core.inject
import timber.log.Timber

/**
 * A ViewModel for updating or creating a new home entry.
 *
 * Created by Thomas Hofmann on 03.07.20
 **/
class UserDialogViewModel : BaseViewModel() {

    private val resourcesProvider: ResourcesProvider by inject()

    val adapter = UserListAdapter()

    fun itemAnimator() = SlideInDownAnimator()

    private val itemListener: (UserListItemViewModel) -> Unit = {
        repository.selectUser(it.item.id)
    }

    private val editListener: (UserListItemViewModel) -> Unit = {
        val arguments = Bundle().apply {
            putBoolean(UserAddEditDialog.UPDATE_KEY, true)
            putString(UserAddEditDialog.TITLE_KEY, it.item.name)
            putLong(HomeAddEditDialog.ID_KEY, it.item.id)
        }
        navigateTo(R.id.dialogUserAddEdit, args = arguments)
    }

    private val deletionListener: () -> Unit = {
        handle(ShowDialogSnack(ShowSnack(
                message = resourcesProvider.getString(R.string.user_delete_undo_title),
                type = Snacker.SnackType.DEFAULT,
                actionText = resourcesProvider.getString(R.string.user_delete_undo_action),
                actionListener = { repository.undoDeleteLastUser() })))
    }

    private val addListener: () -> Unit = {
        navigateTo(R.id.dialogUserAddEdit)
    }

    init {
        repository.observeUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ users ->
                    val items = users.map { user ->
                        UserListItemViewModel(user, null, itemListener, editListener, deletionListener)
                    }.toMutableList()
                    // add more item
                    items.add(UserListItemViewModel(UserDao(-1), addListener, null, null, null))
                    adapter.setItems(items)
                }, { error ->
                    Timber.e(error, "While observing user data.")
                }).addTo(compositeDisposable)
    }

    fun layoutManager() = LinearLayoutManager(resourcesProvider.getApplicationContext())

    fun onClose() {
        handle(Close())
    }

    class Close
    class ShowDialogSnack(val snack: ShowSnack)
}