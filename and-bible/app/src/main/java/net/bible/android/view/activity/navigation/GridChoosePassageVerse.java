package net.bible.android.view.activity.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import net.bible.android.control.navigation.NavigationControl;
import net.bible.android.control.page.window.ActiveWindowPageManagerProvider;
import net.bible.android.view.activity.base.CustomTitlebarActivityBase;
import net.bible.android.view.util.buttongrid.ButtonGrid;
import net.bible.android.view.util.buttongrid.ButtonGrid.ButtonInfo;
import net.bible.android.view.util.buttongrid.OnButtonGridActionListener;

import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Choose a chapter to view
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class GridChoosePassageVerse extends CustomTitlebarActivityBase implements OnButtonGridActionListener {
	
	private BibleBook mBibleBook=BibleBook.GEN;
	private int mBibleChapterNo=1;

	private NavigationControl navigationControl;

	private ActiveWindowPageManagerProvider activeWindowPageManagerProvider;

	private static final String TAG = "GridChoosePassageChaptr";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// background goes white in some circumstances if theme changes so prevent theme change
    	setAllowThemeChange(false);
        super.onCreate(savedInstanceState);

		buildActivityComponent().inject(this);

        int bibleBookNo = getIntent().getIntExtra(GridChoosePassageBook.BOOK_NO, navigationControl.getDefaultBibleBookNo());
        //TODO av11n - this is done now
        mBibleBook = BibleBook.values()[bibleBookNo];

        mBibleChapterNo = getIntent().getIntExtra(GridChoosePassageBook.CHAPTER_NO, navigationControl.getDefaultBibleChapterNo());
        
        // show chosen book in page title to confirm user choice
        try {
            //TODO av11n - done
        	setTitle(navigationControl.getVersification().getLongName(mBibleBook)+" "+mBibleChapterNo);
        } catch (Exception nsve) {
        	Log.e(TAG, "Error in selected book no or chapter no", nsve);
        }
        
        ButtonGrid grid = new ButtonGrid(this);
        grid.setOnButtonGridActionListener(this);
        
        grid.addButtons(getBibleVersesButtonInfo(mBibleBook, mBibleChapterNo));
        setContentView(grid);
    }
    
    private List<ButtonInfo> getBibleVersesButtonInfo(BibleBook book, int chapterNo) {
    	int verses;
    	try {
	    	verses = navigationControl.getVersification().getLastVerse(book, chapterNo);
		} catch (Exception nsve) {
			Log.e(TAG, "Error getting number of verses", nsve);
			verses = -1;
		}
    	
    	List<ButtonInfo> keys = new ArrayList<>();
    	for (int i=1; i<=verses; i++) {
    		ButtonInfo buttonInfo = new ButtonInfo();
			// this is used for preview
			buttonInfo.id = i;
    		buttonInfo.name = Integer.toString(i);
    		keys.add(buttonInfo);
    	}
    	return keys;
    }
    
	@Override
	public void buttonPressed(ButtonInfo buttonInfo) {
		int verse = buttonInfo.id;
		Log.d(TAG, "Verse selected:"+verse);
		try {
			activeWindowPageManagerProvider.getActiveWindowPageManager().getCurrentPage().setKey(new Verse(navigationControl.getVersification(), mBibleBook, mBibleChapterNo, verse));
			onSave(null);

		} catch (Exception e) {
			Log.e(TAG, "error on select of bible book", e);
		}
	}

    public void onSave(View v) {
    	Log.i(TAG, "CLICKED");
    	Intent resultIntent = new Intent(this, GridChoosePassageBook.class);
    	setResult(Activity.RESULT_OK, resultIntent);
    	finish();    
    }

	@Inject
	void setNavigationControl(NavigationControl navigationControl) {
		this.navigationControl = navigationControl;
	}

	@Inject
	void setActiveWindowPageManagerProvider(ActiveWindowPageManagerProvider activeWindowPageManagerProvider) {
		this.activeWindowPageManagerProvider = activeWindowPageManagerProvider;
	}
}
