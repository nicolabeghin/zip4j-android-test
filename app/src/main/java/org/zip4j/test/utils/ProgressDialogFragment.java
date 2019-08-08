package org.zip4j.test.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import org.zip4j.test.MainActivity;
import org.zip4j.test.R;


/**
 * @url https://gist.github.com/leinardi/8cc87f52e7db96aca6b59bbeac2282a8
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String TAG = ProgressDialogFragment.class.getName();
    private static final String KEY_TITLE = TAG + "_message";
    private static final String KEY_MESSAGE = TAG + "_title";

    /**
     * Create and show a ProgressDialogFragment with the given message.
     *
     * @param fragmentManager The FragmentManager this fragment will be added to
     * @param message         displayed message
     */
    public static void showProgressDialog(FragmentManager fragmentManager, CharSequence message) {
        showProgressDialog(fragmentManager, message, null);
    }

    /**
     * Create and show a ProgressDialogFragment with the given message.
     *
     * @param fragmentManager The FragmentManager this fragment will be added to
     * @param message         displayed message
     * @param title           displayed title
     */
    public static void showProgressDialog(FragmentManager fragmentManager, @NonNull CharSequence message, CharSequence title) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) fragmentManager.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = new ProgressDialogFragment();
            final Bundle args = new Bundle();
            args.putCharSequence(KEY_MESSAGE, message);
            if (title != null) {
                args.putCharSequence(KEY_TITLE, title);
            }
            dialogFragment.setArguments(args);
            dialogFragment.show(fragmentManager, TAG);
        } else {
            updateDialog(dialogFragment, message, title);
        }
    }

    private static void updateDialog(ProgressDialogFragment dialogFragment, CharSequence message, CharSequence title) {
        Dialog dialog = dialogFragment.getDialog();
        AppCompatTextView textView = dialog.findViewById(R.id.message);
        textView.setText(message);
        if (title != null) {
            dialog.setTitle(title);
        }
    }

    /**
     * Dismiss an already shown ProgressDialogFragment
     *
     * @param fragmentManager The FragmentManager this fragment will be added to
     */
    public static void dismissProgressDialog(FragmentManager fragmentManager) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) fragmentManager.findFragmentByTag(TAG);
        if (dialogFragment != null) {
            dialogFragment.dismissAllowingStateLoss();
        }
    }

    /**
     * @param manager
     * @param tag
     * @url https://stackoverflow.com/a/35431196/2378095
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e(MainActivity.APP_NAME, "Unable to show progress dialog - " + e.getMessage());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments() == null ? Bundle.EMPTY : getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = LayoutInflater.from(builder.getContext());
        final FrameLayout root = (FrameLayout) inflater.inflate(R.layout.fragment_progressdialog, null /* root */);
        final AppCompatTextView textView = root.findViewById(R.id.message);
        textView.setText(args.getCharSequence(KEY_MESSAGE));
        final CharSequence titleLabel = args.getCharSequence(KEY_TITLE);
        if (titleLabel != null) {
            builder.setTitle(titleLabel);
        }
        builder.setView(root);
        setCancelable(false);
        return builder.create();
    }
}