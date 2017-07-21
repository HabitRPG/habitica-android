package com.habitrpg.android.habitica.ui.views.shops;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.views.CurrencyView;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by phillip on 21.07.17.
 */

public class PurchaseDialog extends AlertDialog {

    @Inject
    UserRepository userRepository;

    @BindView(R.id.currencyView)
    CurrencyView currencyView;
    @BindView(R.id.limitedTextView)
    TextView limitedTextView;
    @BindView(R.id.priceLabel)
    TextView priceLabel;
    @BindView(R.id.currency_icon_view)
    ImageView currencyIconView;
    @BindView(R.id.buyButton)
    View buyButton;
    private ShopItem shopItem;

    public PurchaseDialog(Context context, AppComponent component, ShopItem item) {
        super(context);

        component.inject(this);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_purchase_shopitem, null);
        ButterKnife.bind(this, view);
        setView(view);

        setShopItem(item);

        userRepository.getUser().subscribe(this::setUser, RxErrorHandler.handleEmptyError());
    }

    private void setUser(User user) {
        currencyView.setGold(user.getStats().getGp());
        currencyView.setGems(user.getGemCount());
        currencyView.setHourglasses(user.getHourglassCount());
    }

    @Override
    public void dismiss() {
        userRepository.close();
        super.dismiss();
    }

    private void setShopItem(ShopItem item) {
        this.shopItem = item;

        buyButton.setVisibility(View.VISIBLE);

        if (item.getUnlockCondition() == null) {
            priceLabel.setText(item.getValue().toString());
            if (item.getCurrency().equals("gold")) {
                currencyIconView.setImageResource(R.drawable.currency_gold);
                priceLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.gold));
            } else if (item.getCurrency().equals("gems")) {
                currencyIconView.setImageResource(R.drawable.currency_gem);
                priceLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.good_10));
            } else {
                buyButton.setVisibility(View.GONE);
            }
        } else {
            buyButton.setVisibility(View.GONE);
        }

        if (item.isLimited()) {
            //TODO: replace with correct date once API is final
            limitedTextView.setText(getContext().getString(R.string.available_until, new Date().toString()));
        } else {
            limitedTextView.setVisibility(View.GONE);
        }

        if (item.getLocked()) {
            priceLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_300));
            currencyIconView.setAlpha(0.5f);
        } else {
            currencyIconView.setAlpha(1.0f);
        }
    }

    @OnClick(R.id.closeButton)
    public void onCloseClicked() {
        dismiss();
    }
}
