package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator;
import com.habitrpg.android.habitica.prefs.scanner.IntentResult;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InboxFragment extends BaseMainFragment
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, HabitRPGUserCallback.OnUserReceived {

    @Inject
    UserRepository userRepository;

    @BindView(R.id.inbox_messages)
    LinearLayout inboxMessagesListView;

    @BindView(R.id.inbox_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    Map<String, ChatMessage> messages;
    List<String> roomsAdded;
    private View chooseRecipientDialogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.apiClient.markPrivateMessagesRead()
                .subscribe(aVoid -> {
                }, throwable -> {
                });

        View v = inflater.inflate(R.layout.fragment_inbox, container, false);
        unbinder = ButterKnife.bind(this, v);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (this.user != null) {
            this.messages = this.user.getInbox().getMessages();
            if (this.messages != null) {
                this.setInboxMessages();
            }
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.activity != null) {
            this.activity.getMenuInflater().inflate(R.menu.inbox, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.send_message:
                openNewMessageDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openNewMessageDialog() {
        assert this.activity != null;
        this.chooseRecipientDialogView = this.activity.getLayoutInflater().inflate(R.layout.dialog_choose_message_recipient, null);

        Button scaneQRCodeButton = (Button) chooseRecipientDialogView.findViewById(R.id.scanQRCodeButton);

        AlertDialog alert = new AlertDialog.Builder(this.activity)
                .setTitle(getString(R.string.choose_recipient_title))
                .setPositiveButton(getString(R.string.action_continue), (dialog, which) -> {
                    EditText uuidEditText = (EditText) chooseRecipientDialogView.findViewById(R.id.uuidEditText);
                    openInboxMessages(uuidEditText.getText().toString(), "");
                })
                .setNeutralButton(getString(R.string.action_cancel), (dialog, which) -> {
                    UiUtils.dismissKeyboard(this.activity);
                    dialog.cancel();
                })
                .create();
        scaneQRCodeButton.setOnClickListener((View v) -> {
            IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
            scanIntegrator.initiateScan(this);
        });
        alert.setView(chooseRecipientDialogView);
        alert.show();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        this.userRepository.retrieveUser(true)
                .subscribe(this::onUserReceived, throwable -> {
                });
    }

    public void setInboxMessages() {
        if (this.inboxMessagesListView == null) {
            return;
        }

        this.inboxMessagesListView.removeAllViewsInLayout();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        roomsAdded = new ArrayList<>();

        if (this.messages.size() > 0) {
            for (Object o : this.messages.entrySet()) {
                Map.Entry pair = (Map.Entry) o;

                ChatMessage message = (ChatMessage) pair.getValue();
                if (roomsAdded.contains(message.uuid)) {
                    TextView entry = (TextView) this.inboxMessagesListView.findViewWithTag(message.uuid);
                    entry.setText(message.user);
                } else {
                    roomsAdded.add(message.uuid);

                    TextView entry = (TextView) inflater.inflate(R.layout.plain_list_item, this.inboxMessagesListView, false);
                    entry.setText(message.user);
                    entry.setTag(message.uuid);
                    entry.setOnClickListener(this);
                    this.inboxMessagesListView.addView(entry);
                }
            }
        } else {
            TextView tv = new TextView(getContext());
            tv.setText(R.string.empty_inbox);
        }
    }

    @Override
    public void onClick(View v) {
        TextView entry = (TextView) v;
        String replyToUserName = entry.getText().toString();
        openInboxMessages(entry.getTag().toString(), replyToUserName);
    }

    private void openInboxMessages(String userID, String username) {
        InboxMessageListFragment inboxMessageListFragment = new InboxMessageListFragment();
        inboxMessageListFragment.setMessages(this.messages, username, userID);
        if (this.activity != null) {
            this.activity.displayFragment(inboxMessageListFragment);
        }
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.messages = user.getInbox().getMessages();
        this.setInboxMessages();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult != null && scanningResult.getContents() != null) {
            if (this.chooseRecipientDialogView != null) {
                EditText uuidEditText = (EditText) this.chooseRecipientDialogView.findViewById(R.id.uuidEditText);
                String qrCodeUrl = scanningResult.getContents();
                Uri uri = Uri.parse(qrCodeUrl);
                if (uri == null || uri.getPathSegments().size() < 3) {
                    return;
                }
                String userID = uri.getPathSegments().get(2);
                uuidEditText.setText(userID);
            }
        }
    }

    @Override
    public String customTitle() {
        return getString(R.string.sidebar_inbox);
    }
}
