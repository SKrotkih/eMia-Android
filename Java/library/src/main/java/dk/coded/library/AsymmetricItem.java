package dk.coded.library;

import android.os.Parcelable;

public interface AsymmetricItem extends Parcelable {
  int getColumnSpan();
  int getRowSpan();
}
