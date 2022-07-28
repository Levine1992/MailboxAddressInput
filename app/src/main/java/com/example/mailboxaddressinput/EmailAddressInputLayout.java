package com.example.mailboxaddressinput;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EmailAddressInputLayout extends FrameLayout implements View.OnFocusChangeListener, View.OnKeyListener, View.OnClickListener {

    //子view高度
    private int viewHeight;
    //子view左外边距
    private int leftMargin;
    private EditText editInputText;
    private CharSequence title = "标题：";
    //是否以文本方式显示账号
    private boolean isTextModeOfAccount = false;
    //显示邮件地址文本的view
    private TextView allAccountTextView;
    private final StringBuilder allAccountStr = new StringBuilder();
    public OnShowAllTextAccountListener onShowAllTextAccountListener;
    public OnAccountChangeListener onAccountChangeListener;
    public TextView titleTextView;
    //默认文本背景色
    private final String defaultAddressTextColor = "#802196F3";
    //获得焦点文本背景色
    private final String focusAddressTextColor = "#2196F3";

    public EmailAddressInputLayout(Context context) {
        super(context);
        init(null);
    }

    public EmailAddressInputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmailAddressInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        viewHeight = dp2px(28);
        leftMargin = dp2px(5);
        addView(createTitleTextView());
        addAllAccountTextView();
        addView(createInputView());
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTextModeOfAccount) return;
                isTextModeOfAccount = false;
                if (onShowAllTextAccountListener != null) {
                    onShowAllTextAccountListener.onShow(false);
                }
                requestLayout();
                showSoftInput(editInputText);
            }
        });
    }

    public void setTitle(String value) {
        titleTextView.setText(value);
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void showSoftInput(@NonNull final View view) {
        InputMethodManager imm =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        imm.showSoftInput(view, 0, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN
                        || resultCode == InputMethodManager.RESULT_HIDDEN) {
                    toggleSoftInput();
                }
            }
        });
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void toggleSoftInput() {
        InputMethodManager imm =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.toggleSoftInput(0, 0);
    }

    private EditText createInputView() {
        editInputText = new EditText(getContext());
        editInputText.setHint("请输入邮箱地址");
        editInputText.setOnKeyListener(this);
        editInputText.setPadding(0, 0, 0, 0);
        editInputText.setMinWidth(dp2px(50));
//        editInputText.setBackgroundColor(Color.WHITE);
        editInputText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editInputText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editInputText.setSingleLine(true);
        editInputText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        LayoutParams layoutParams = new LayoutParams(-2, viewHeight);
        editInputText.setGravity(Gravity.CENTER_VERTICAL);
        editInputText.setLayoutParams(layoutParams);
        editInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    if (editInputText.getText().length() == 0) return true;
                    if (!isEmail(editInputText.getText().toString())) {
                        showToast("请输入正确的邮箱地址");
                        return true;
                    }
                    addAddress(new Address(editInputText.getText().toString()));
                    requestLayout();
                    editInputText.setText("");
                    return true;
                }
                return false;
            }
        });
        editInputText.setOnFocusChangeListener(this);
        return editInputText;
    }

    private void showToast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();

    }

    public boolean isEmail(String email) {
        if (email == null || email.length() < 1 || email.length() > 256) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(email).matches();
    }


    public void addAllAccountTextView() {
        allAccountTextView = new TextView(getContext());
        allAccountTextView.setMaxLines(1);
        allAccountTextView.setTextColor(Color.BLACK);
        allAccountTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        allAccountTextView.setEllipsize(TextUtils.TruncateAt.END);
        allAccountTextView.setGravity(Gravity.CENTER_VERTICAL);
        allAccountTextView.setLayoutParams(new LayoutParams(-1, viewHeight));
        addView(allAccountTextView);
    }

    public void addAddressList(List<Address> addressList) {
        for (Address address : addressList) {
            addAddress(address);
        }
        postDelayed(this::requestLayout, 100);
    }

    public void addAddress(Address address) {
        if (isRepeat(address.address) != null) return;
        TextView textView = createAddressTextView();
        textView.setText(TextUtils.isEmpty(address.name) ? address.address : address.name);
        textView.setTag(address);
        addViewInLayout(textView, getChildCount() - 1, new LayoutParams(-2, viewHeight), true);
        onAccountChange(false, textView);
    }

    private void onAccountChange(boolean isRemove, View view) {
        postDelayed(() -> {
            if (onAccountChangeListener != null) {
                onAccountChangeListener.onChange(isRemove, (Address) view.getTag(), getAddressList());
            }
        }, 100);

    }

    private TextView createAddressTextView() {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.parseColor(defaultAddressTextColor));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setPadding(dp2px(10), 0, dp2px(10), 0);
        textView.setOnFocusChangeListener(EmailAddressInputLayout.this);
        textView.setOnKeyListener(EmailAddressInputLayout.this);
        textView.setOnClickListener(EmailAddressInputLayout.this);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, viewHeight);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    public List<Address> getAddressList() {
        List<Address> addressList = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view.getTag() instanceof Address) {
                Address address = (Address) view.getTag();
                addressList.add(address);
            }
        }
        return addressList;
    }

    /**
     * 是否重复
     */
    private View isRepeat(String address) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view.getTag() instanceof Address)) continue;
            if (((Address) view.getTag()).address.equals(address)) {
                showToast("存在重复邮箱");
                return view;
            }
        }
        return null;
    }

    private TextView createTitleTextView() {
        titleTextView = new TextView(getContext());
        titleTextView.setText(title);
        titleTextView.setTextColor(Color.GRAY);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        LayoutParams layoutParams = new LayoutParams(-2, viewHeight);
        titleTextView.setGravity(Gravity.CENTER_VERTICAL);
        titleTextView.setLayoutParams(layoutParams);
        return titleTextView;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int top = 0;
        int right = 0;
        int lineMaxBottom = 0;
        int allAccountViewWidth = 0;

        if (isTextModeOfAccount) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (i == 0) {
                    allAccountViewWidth = measuredWidth - view.getMeasuredWidth();
                    lineMaxBottom = view.getMeasuredHeight();
                }
                if (view == allAccountTextView) {
                    view.getLayoutParams().width = allAccountViewWidth;
                    measureChild(view, widthMeasureSpec, heightMeasureSpec);
                }
            }
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view == allAccountTextView) continue;
                if (i == getChildCount() - 1) {
                    view.getLayoutParams().width = dp2px(50);
                    measureChild(view, widthMeasureSpec, heightMeasureSpec);
                }
                if (right + view.getMeasuredWidth() + (i < 1 ? 0 : leftMargin) > measuredWidth) {
                    right = 0;
                    top = top + lineMaxBottom + leftMargin;
                }
                if (top + getMeasuredHeight() > lineMaxBottom) {
                    lineMaxBottom = getMeasuredHeight();
                }
                if (i == getChildCount() - 1) {
                    view.getLayoutParams().width = measuredWidth - right;
                    measureChild(view, widthMeasureSpec, heightMeasureSpec);
                }
                right = right + view.getMeasuredWidth() + (i < 1 ? 0 : leftMargin);
            }
        }
        setMeasuredDimension(measuredWidth, top + lineMaxBottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        int viewMaxBottom = 0;

        if (isTextModeOfAccount) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (i > 0 && view != allAccountTextView) {
                    view.layout(0, 0, 0, 0);
                    if (view == editInputText) {
                        view.layout(0, 0, dp2px(1), view.getMeasuredHeight());
                    }
                    continue;
                }
                view.layout(viewLeft, viewTop, viewLeft + view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
                viewLeft = viewLeft + view.getMeasuredWidth() + (i < 1 ? 0 : leftMargin);
            }
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view == allAccountTextView) {
                    view.layout(0, 0, 0, 0);
                    continue;
                }
                if (viewLeft + view.getMeasuredWidth() > getMeasuredWidth()) {
                    viewLeft = 0;
                    viewTop = viewTop + view.getMeasuredHeight() + leftMargin;
                }
                if (viewTop + view.getMeasuredHeight() > viewMaxBottom) {
                    viewMaxBottom = viewTop + view.getMeasuredHeight();
                }
                view.layout(viewLeft, viewTop, viewLeft + view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
                viewLeft = viewLeft + view.getMeasuredWidth() + (i < 1 ? 0 : leftMargin);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            titleTextView.setTextColor(Color.BLACK);
            titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            titleTextView.setTextColor(Color.GRAY);
            titleTextView.setTypeface(Typeface.DEFAULT);
        }
        if (view instanceof TextView && view.getTag() instanceof Address) {
            TextView textView = (TextView) view;
            if (b) {
                view.setBackgroundColor(Color.parseColor(focusAddressTextColor));
                textView.setTextColor(Color.WHITE);
            } else {
                view.setBackgroundColor(Color.parseColor(defaultAddressTextColor));
                textView.setTextColor(Color.WHITE);
            }
        }
        if (view == editInputText && editInputText.getText().length() > 0 && !b) {
            if (!isEmail(editInputText.getText().toString())) {
                showToast("请输入正确的邮箱地址");
            } else {
                addAddress(new Address(editInputText.getText().toString()));
                requestLayout();
            }
        }
        if (b) {
            if (onShowAllTextAccountListener != null) {
                onShowAllTextAccountListener.onShow(false);
            }
            return;
        }
        checkAllViewHasFocus();
    }

    private void checkAllViewHasFocus() {
        //检查子view是否都失去了焦点
        isTextModeOfAccount = false;
        boolean hasFocused = false;
        allAccountStr.delete(0, allAccountStr.length());
        for (int i = 0; i < getChildCount(); i++) {
            if (i == 0) continue;
            View child = getChildAt(i);
            if (child instanceof TextView && child.getTag() instanceof Address) {
                TextView textView = (TextView) child;
                if (allAccountStr.length() > 0) allAccountStr.append(";");
                allAccountStr.append(textView.getText());
            }
            if (child.isFocused()) {
                hasFocused = true;
            }
        }
        if (!hasFocused) {
            //所有view失去焦点将所填账号以文本方式显示出来
            textModeShowOfAccount();
        }
        requestLayout();
    }

    /**
     * 以文本方式显示账号
     */
    public void textModeShowOfAccount() {
        allAccountStr.delete(0, allAccountStr.length());
        for (int i = 0; i < getChildCount(); i++) {
            if (i == 0) continue;
            View child = getChildAt(i);
            if (child instanceof TextView && child.getTag() instanceof Address) {
                TextView textView = (TextView) child;
                if (allAccountStr.length() > 0) allAccountStr.append(";");
                allAccountStr.append(textView.getText());
            }
        }
        allAccountTextView.setText(allAccountStr);
        editInputText.setText("");
        isTextModeOfAccount = true;
        if (onShowAllTextAccountListener != null) onShowAllTextAccountListener.onShow(true);
        requestLayout();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (getChildCount() <= 2) return false;
        if (i == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (view instanceof EditText) {
                Editable editable = editInputText.getText();
                int length = editable.length();
                if (length == 0) {
                    View childAt = getChildAt(getChildCount() - 2);
                    childAt.setFocusable(true);
                    if (childAt.isFocused()) {
                        removeView(childAt);
                    } else {
                        showSoftInput(childAt);
                    }
                    return true;
                }
                return false;
            }
            if (view instanceof TextView && view.getTag() instanceof Address && view.isFocused()) {
                removeAccount(view);
            }
        }
        return false;
    }

    private void removeAccount(View view) {
        view.setOnFocusChangeListener(null);
        removeView(view);
        onAccountChange(true, view);
        showSoftInput(editInputText);
    }

    @Override
    public void onClick(View view) {
        showSoftInput(view);
    }

    public interface OnShowAllTextAccountListener {
        void onShow(boolean isShow);
    }

    public interface OnAccountChangeListener {
        void onChange(boolean isRemove, Address address, List<Address> addressList);
    }

    public static class Address {
        public String name;
        public String address;

        public Address(String address) {
            this.address = address;
        }

        public Address(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }
}
