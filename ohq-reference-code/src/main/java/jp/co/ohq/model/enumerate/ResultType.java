package jp.co.ohq.model.enumerate;


import androidx.annotation.StringRes;

import jp.co.ohq.ble.reference.code.R;

public enum ResultType {
    Success(R.string.success),
    Failure(R.string.failure);
    @StringRes
    int id;

    ResultType(@StringRes int id) {
        this.id = id;
    }

    @StringRes
    public int stringResId() {
        return this.id;
    }
}
