package com.habitrpg.android.habitica.ui.views.shops;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.views.CurrencyView;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class PurchaseDialog extends AlertDialog {

    @Inject
    UserRepository userRepository;
    @Inject
    InventoryRepository inventoryRepository;

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
    @BindView(R.id.content_container)
    ViewGroup contentContainer;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    private ShopItem shopItem;

    private PurchaseDialogContent contentView;

    private CompositeSubscription compositeSubscription;
    public String shopIdentifier;

    public PurchaseDialog(Context context, AppComponent component, ShopItem item) {
        super(context);

        component.inject(this);

        compositeSubscription = new CompositeSubscription();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_purchase_shopitem, null);
        ButterKnife.bind(this, view);
        setView(view);

        setShopItem(item);

        compositeSubscription.add(userRepository.getUser().subscribe(this::setUser, RxErrorHandler.handleEmptyError()));
    }

    private void setUser(User user) {
        currencyView.setGold(user.getStats().getGp());
        currencyView.setGems(user.getGemCount());
        currencyView.setHourglasses(user.getHourglassCount());

        if ("gems".equals(shopItem.purchaseType)) {
            int gemsLeft = shopItem.limitedNumberLeft != null ? shopItem.limitedNumberLeft : 0;
            int maxGems = user.getPurchased().getPlan().totalNumberOfGems();
            if (maxGems > 0) {
                limitedTextView.setText(getContext().getString(R.string.gems_left_max, gemsLeft, maxGems));
            } else {
                limitedTextView.setText(getContext().getString(R.string.gems_left_nomax, gemsLeft));
            }
            limitedTextView.setVisibility(View.VISIBLE);
            limitedTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.good_10));
        }
    }

    @Override
    public void dismiss() {
        userRepository.close();
        inventoryRepository.close();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
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
            } else if (item.getCurrency().equals("hourglasses")) {
                currencyIconView.setImageResource(R.drawable.currency_hourglass);
                priceLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_300));
            } else {
                setBuyButtonEnabled(false);
            }
        } else {
            setBuyButtonEnabled(false);
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

        if (shopItem.isTypeItem()) {
            contentView = new PurchaseDialogItemContent(getContext());
        } else if (shopItem.isTypeQuest()) {
            contentView = new PurchaseDialogQuestContent(getContext());
            inventoryRepository.getQuestContent(item.getKey()).first().subscribe(((PurchaseDialogQuestContent) contentView)::setQuestContent, RxErrorHandler.handleEmptyError());
        } else if (shopItem.isTypeGear()) {
            contentView = new PurchaseDialogGearContent(getContext());
            inventoryRepository.getEquipment(item.getKey()).first().subscribe(((PurchaseDialogGearContent) contentView)::setEquipment, RxErrorHandler.handleEmptyError());
        } else if ("gems".equals(shopItem.purchaseType)) {
            contentView = new PurchaseDialogGemsContent(getContext());
        } else {
            contentView = new PurchaseDialogBaseContent(getContext());
        }
        contentView.setItem(shopItem);
        contentContainer.addView(contentView);


        setScrollviewSize();
    }

    private void setScrollviewSize() {
        scrollView.post(() -> {
            if (getWindow() != null) {
                int height = scrollView.getChildAt(0).getHeight();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;
                int spaceRequired = (int) (displayMetrics.density * 160);

                if (height > screenHeight-spaceRequired) {
                    ViewGroup.LayoutParams myScrollViewParams = scrollView.getLayoutParams();
                    myScrollViewParams.height = screenHeight-spaceRequired;
                    scrollView.setLayoutParams(myScrollViewParams);

                }
            }
        });

    }


    @OnClick(R.id.closeButton)
    void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.buyButton)
    void onBuyButtonClicked() {
        BuyGemItemCommand event = new BuyGemItemCommand();
        event.shopIdentifier = shopIdentifier;
        event.item = shopItem;
        EventBus.getDefault().post(event);
        dismiss();
    }

    public void setBuyButtonEnabled(boolean enabled) {
        if (enabled) {
            buyButton.setAlpha(0.5f);
        } else{
            buyButton.setAlpha(1.0f);
        }
    }
}
