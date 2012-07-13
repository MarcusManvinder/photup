package uk.co.senab.photup;

import uk.co.senab.photup.fragments.SelectedPhotosFragment;
import uk.co.senab.photup.fragments.UploadsFragment;
import uk.co.senab.photup.fragments.UserPhotosFragment;
import uk.co.senab.photup.listeners.OnPhotoSelectionChangedListener;
import uk.co.senab.photup.model.PhotoSelection;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class PhotoSelectionActivity extends SherlockFragmentActivity implements OnPhotoSelectionChangedListener,
		TabListener {

	static final int TAB_PHOTOS = 0;
	static final int TAB_SELECTED = 1;
	static final int TAB_UPLOADS = 2;

	private PhotoUploadController mPhotoController;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_photos);

		mPhotoController = PhotoUploadController.getFromContext(this);
		mPhotoController.addPhotoSelectionListener(this);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ab.addTab(ab.newTab().setText(R.string.tab_photos).setTag(TAB_PHOTOS).setTabListener(this));
		ab.addTab(ab.newTab().setText(getSelectedTabTitle()).setTag(TAB_SELECTED).setTabListener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getSupportActionBar().getSelectedNavigationIndex() < 2) {
			getSupportMenuInflater().inflate(R.menu.menu_photo_grid, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				startActivity(new Intent(this, LoginActivity.class));
				return true;
			case R.id.menu_upload:
				if (mPhotoController.getSelectedPhotoUploadsSize() == 0) {
					Toast.makeText(this, R.string.error_select_photos, Toast.LENGTH_SHORT).show();
				} else {
					startActivity(new Intent(this, UploadActivity.class));
				}
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mPhotoController.hasUploads()) {
			addUploadTab();
		}

		if (mPhotoController.getActiveUploadsSize() > 0) {
			getSupportActionBar().setSelectedNavigationItem(2);
		} else if (mPhotoController.getSelectedPhotoUploadsSize() == 0) {
			getSupportActionBar().setSelectedNavigationItem(0);
		}
	}

	public void onSelectionsAddedToUploads() {
		addUploadTab();
		refreshSelectedTabTitle();
	}

	public void onPhotoSelectionChanged(PhotoSelection upload, boolean added) {
		refreshSelectedTabTitle();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPhotoController.removePhotoSelectionListener(this);
	}

	private Tab mPreviouslySelectedTab;

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		final int id = (Integer) tab.getTag();
		Fragment fragment;

		switch (id) {
			case TAB_SELECTED:
				fragment = new SelectedPhotosFragment();
				break;
			case TAB_UPLOADS:
				fragment = new UploadsFragment();
				break;
			case TAB_PHOTOS:
			default:
				fragment = new UserPhotosFragment();
				break;
		}

		if (null != mPreviouslySelectedTab) {
			final int oldId = (Integer) mPreviouslySelectedTab.getTag();
			final int enterAnim = id > oldId ? R.anim.slide_in_right : R.anim.slide_in_left;
			final int exitAnim = id > oldId ? R.anim.slide_out_left : R.anim.slide_out_right;
			ft.setCustomAnimations(enterAnim, exitAnim);
		}

		ft.replace(R.id.fl_fragment, fragment);
		supportInvalidateOptionsMenu();
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		mPreviouslySelectedTab = tab;
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// NO-OP
	}

	private void refreshSelectedTabTitle() {
		getSupportActionBar().getTabAt(1).setText(getSelectedTabTitle());
	}

	private CharSequence getSelectedTabTitle() {
		return getString(R.string.tab_selected_photos, mPhotoController.getSelectedPhotoUploadsSize());
	}

	private void addUploadTab() {
		ActionBar ab = getSupportActionBar();

		// Bit of a hack as but we expect the upload tab to be the third
		if (ab.getTabCount() == 2) {
			ab.addTab(ab.newTab().setText(R.string.tab_uploads).setTag(TAB_UPLOADS).setTabListener(this));
		}
	}

	public void onUploadsCleared() {
		getSupportActionBar().setSelectedNavigationItem(0);
		getSupportActionBar().removeTabAt(2);
	}

}
