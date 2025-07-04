// Generated by view binder compiler. Do not edit!
package com.sohaibazmi.remote.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.sohaibazmi.remote.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityQrResultBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView txtResult;

  private ActivityQrResultBinding(@NonNull LinearLayout rootView, @NonNull TextView txtResult) {
    this.rootView = rootView;
    this.txtResult = txtResult;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityQrResultBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityQrResultBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_qr_result, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityQrResultBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.txt_result;
      TextView txtResult = ViewBindings.findChildViewById(rootView, id);
      if (txtResult == null) {
        break missingId;
      }

      return new ActivityQrResultBinding((LinearLayout) rootView, txtResult);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
