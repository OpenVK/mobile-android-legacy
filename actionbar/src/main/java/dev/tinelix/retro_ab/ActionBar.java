/*
 * Copyright (C) 2010 Johan Nilsson <http://markupartist.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.tinelix.retro_ab;

import java.util.LinkedList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.tinelix.retro_pm.PopupMenu;

public class ActionBar extends RelativeLayout implements OnClickListener {

    private final Context mContext;
    private LayoutInflater mInflater;
    private RelativeLayout mBarView;
    private ImageView mLogoView;
    private ImageView mRightLogoView;
    private View mBackIndicator;
    //private View mHomeView;
    private TextView mTitleView;
    private TextView mSubtitleView;
    private LinearLayout mActionsView;
    private ImageButton mHomeBtn;
    private RelativeLayout mHomeLayout;
    private ProgressBar mProgress;

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBarView = (RelativeLayout) mInflater.inflate(R.layout.actionbar, null);
        addView(mBarView);

        mLogoView = (ImageView) mBarView.findViewById(R.id.actionbar_home_logo);
        mRightLogoView = (ImageView) mBarView.findViewById(R.id.actionbar_right_logo);
        mHomeLayout = (RelativeLayout) mBarView.findViewById(R.id.actionbar_home_bg);
        mHomeBtn = (ImageButton) mBarView.findViewById(R.id.actionbar_home_btn);
        mBackIndicator = mBarView.findViewById(R.id.actionbar_home_is_back);

        mTitleView = (TextView) mBarView.findViewById(R.id.actionbar_title);
        mSubtitleView = (TextView) mBarView.findViewById(R.id.actionbar_subtitle);
        mActionsView = (LinearLayout) mBarView.findViewById(R.id.actionbar_actions);

        mProgress = (ProgressBar) mBarView.findViewById(R.id.actionbar_progress);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ActionBar);
        CharSequence title = a.getString(R.styleable.ActionBar_title);
        if (title != null) {
            setTitle(title);
        }
        a.recycle();
    }

    public void setHomeAction(Action action) {
        mHomeBtn.setOnClickListener(this);
        mHomeBtn.setTag(action);
        mHomeBtn.setImageResource(action.getDrawable());
        mHomeLayout.setVisibility(View.VISIBLE);
    }

    public void clearHomeAction() {
        mHomeLayout.setVisibility(View.GONE);
    }

    /**
     * Shows the provided logo to the left or right in the action bar.
     *
     * This is meant to be used instead of the setHomeAction and does not draw
     * a divider to the left of the provided logo.
     *
     * @param resId The drawable resource id
     */
    public void setHomeLogo(int resId) {
        // TODO: Add possibility to add an IntentAction as well.
        mLogoView.setImageResource(resId);
        mLogoView.setVisibility(View.VISIBLE);
        mHomeLayout.setVisibility(View.GONE);
    }

    public void setHomeLogo(BitmapDrawable bitmapDrawable) {
        mLogoView.setImageDrawable(bitmapDrawable);
        mLogoView.setVisibility(View.VISIBLE);
        mHomeLayout.setVisibility(View.GONE);
    }

    public void setRightLogo(int resId) {
        mRightLogoView.setImageResource(resId);
        mRightLogoView.setVisibility(View.VISIBLE);
    }

    public void setRightLogo(BitmapDrawable bitmapDrawable) {
        mRightLogoView.setImageDrawable(bitmapDrawable);
        mRightLogoView.setVisibility(View.VISIBLE);
    }

    /* Emulating Honeycomb, setdisplayHomeAsUpEnabled takes a boolean
     * and toggles whether the "home" view should have a little triangle
     * indicating "up" */
    public void setDisplayHomeAsUpEnabled(boolean show) {
        mBackIndicator.setVisibility(show? View.VISIBLE : View.GONE);
    }


    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }

    public void setTitle(int resid) {
        mTitleView.setText(resid);
    }

    public void setSubtitle(CharSequence subtitle) {
        mSubtitleView.setText(subtitle);
    }

    public void setSubtitle(int resid) {
        mSubtitleView.setText(resid);
        if(getResources().getString(resid).length() > 0)
            mSubtitleView.setVisibility(VISIBLE);
        else mSubtitleView.setVisibility(GONE);
    }

    /**
     * Set the enabled state of the progress bar.
     *
     * @param One of {@link View#VISIBLE}, {@link View#INVISIBLE},
     *   or {@link View#GONE}.
     */
    public void setProgressBarVisibility(int visibility) {
        mProgress.setVisibility(visibility);
    }

    /**
     * Returns the visibility status for the progress bar.
     *
     * @param One of {@link View#VISIBLE}, {@link View#INVISIBLE},
     *   or {@link View#GONE}.
     */
    public int getProgressBarVisibility() {
        return mProgress.getVisibility();
    }

    /**
     * Function to set a click listener for Title TextView
     *
     * @param listener the onClickListener
     */
    public void setOnTitleClickListener(OnClickListener listener) {
        mTitleView.setOnClickListener(listener);
    }

    @Override
    public void onClick(View view) {
        final Object tag = view.getTag();
        if (tag instanceof Action) {
            final Action action = (Action) tag;
            action.performAction(view);
        }
    }

    /**
     * Adds a list of {@link Action}s.
     * @param actionList the actions to add
     */
    public void addActions(ActionList actionList) {
        int actions = actionList.size();
        for (int i = 0; i < actions; i++) {
            addAction(actionList.get(i));
        }
    }

    /**
     * Adds a new {@link Action}.
     * @param action the action to add
     */
    public void addAction(Action action) {
        final int index = mActionsView.getChildCount();
        addAction(action, index);
    }

    /**
     * Adds a new {@link Action} at the specified index.
     * @param action the action to add
     * @param index the position at which to add the action
     */
    public void addAction(Action action, int index) {
        mActionsView.addView(inflateAction(action), index);
    }

    /**
     * Removes all action views from this action bar
     */
    public void removeAllActions() {
        mActionsView.removeAllViews();
    }

    /**
     * Remove a action from the action bar.
     * @param index position of action to remove
     */
    public void removeActionAt(int index) {
        mActionsView.removeViewAt(index);
    }

    /**
     * Remove a action from the action bar.
     * @param action The action to remove
     */
    public void removeAction(Action action) {
        int childCount = mActionsView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mActionsView.getChildAt(i);
            if (view != null) {
                final Object tag = view.getTag();
                if (tag instanceof Action && tag.equals(action)) {
                    mActionsView.removeView(view);
                }
            }
        }
    }

    /**
     * Returns the number of actions currently registered with the action bar.
     * @return action count
     */
    public int getActionCount() {
        return mActionsView.getChildCount();
    }

    /**
     * Inflates a {@link View} with the given {@link Action}.
     * @param action the action to inflate
     * @return a view
     */
    private View inflateAction(Action action) {
        View view = mInflater.inflate(R.layout.actionbar_item, mActionsView, false);

        ImageButton labelView =
                (ImageButton) view.findViewById(R.id.actionbar_item);
        labelView.setImageResource(action.getDrawable());

        view.setTag(action);
        view.setOnClickListener(this);
        return view;
    }

    /**
     * A {@link LinkedList} that holds a list of {@link Action}s.
     */
    public static class ActionList extends LinkedList<Action> {
    }

    /**
     * Definition of an action that could be performed, along with a icon to
     * show.
     */
    public interface Action {
        public int getDrawable();
        public void performAction(View view);
    }

    public static abstract class AbstractAction implements Action {
        final private int mDrawable;

        public AbstractAction(int drawable) {
            mDrawable = drawable;
        }

        @Override
        public int getDrawable() {
            return mDrawable;
        }
    }

    public static class IntentAction extends AbstractAction {
        private Context mContext;
        private Intent mIntent;

        public IntentAction(Context context, Intent intent, int drawable) {
            super(drawable);
            mContext = context;
            mIntent = intent;
        }

        @Override
        public void performAction(View view) {
            try {
                mContext.startActivity(mIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext,
                        mContext.getText(R.string.actionbar_activity_not_found),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class PopupMenuAction extends AbstractAction {
        private final CharSequence mTitle;
        private Context mContext;
        private Intent mIntent;
        private PopupMenu.OnItemSelectedListener mItemListener;
        private PopupMenu p_menu;
        private Menu menu;

        public PopupMenuAction(Context context, CharSequence title, Menu menu, int drawable, PopupMenu.OnItemSelectedListener mItemListener) {
            super(drawable);
            this.menu = menu;
            mContext = context;
            mTitle = title;
            this.mItemListener = mItemListener;
        }

        @Override
        public void performAction(View view) {
            try {
                if(mContext != null) {
                    // integration w/ custom popup menu
                    p_menu = new PopupMenu(mContext);
                    p_menu.setHeaderTitle(mTitle);
                    p_menu.setOnItemSelectedListener(mItemListener);
                    for(int i = 0; i < menu.size(); i++) {
                        if(menu.getItem(i).isVisible() && menu.getItem(i).isEnabled()) {
                            p_menu.add(i, (String) menu.getItem(i).getTitle());
                        }
                    }
                    p_menu.show(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext,
                        mContext.getText(R.string.actionbar_activity_not_found),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setCustomView(View view) {
        LinearLayout custom_layout = findViewById(R.id.custom_layout);
        if(view != null) {
            findViewById(R.id.actionbar_home_btn).setVisibility(GONE);
            int ab_height = this.getLayoutParams().height;
            if (custom_layout.getChildCount() == 0) {
                custom_layout.addView(view, 0);
            } else if(view != getChildAt(0)) {
                custom_layout.removeViewAt(0);
                custom_layout.addView(view, 0);
            }
            custom_layout.setVisibility(VISIBLE);
            findViewById(R.id.actionbar_title_container).setVisibility(GONE);
        } else {
            findViewById(R.id.actionbar_home_btn).setVisibility(VISIBLE);
            custom_layout.setVisibility(GONE);
        }
    }

    /*
    public static abstract class SearchAction extends AbstractAction {
        public SearchAction() {
            super(R.drawable.actionbar_search);
        }
    }
    */
}
