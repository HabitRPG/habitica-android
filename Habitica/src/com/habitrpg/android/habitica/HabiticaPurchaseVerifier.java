package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Transaction;

import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Negue on 26.11.2015.
 */
public class HabiticaPurchaseVerifier extends BasePurchaseVerifier {

    @Override
    protected void doVerify(final List<Purchase> purchases, final RequestListener<List<Purchase>> requestListener) {
        final List<Purchase> verifiedPurchases = new ArrayList<Purchase>(purchases.size());

        for(final Purchase purchase : purchases)
        {
            PurchaseValidationRequest validationRequest = new PurchaseValidationRequest();
            validationRequest.transaction = new Transaction();
            validationRequest.transaction.receipt = purchase.data;
            validationRequest.transaction.signature = purchase.signature;

            PurchaseValidationResult purchaseValidationResult = HabiticaApplication.ApiHelper.validatePurchase(validationRequest);

            if (purchaseValidationResult.ok) {
                verifiedPurchases.add(purchase);

                requestListener.onSuccess(verifiedPurchases);
            }
            else
            {
                requestListener.onError(purchases.indexOf(purchase), new Exception(purchaseValidationResult.data.toString()));
            }

        }
    }
}
