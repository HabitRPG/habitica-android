package com.habitrpg.android.habitica.helpers;

import java.util.Arrays;
import java.util.List;

public class PurchaseTypes {

    public static String Purchase4Gems = "com.habitrpg.android.habitica.iap.4gems";
    public static String Purchase21Gems = "com.habitrpg.android.habitica.iap.21gems";
    public static String Purchase42Gems = "com.habitrpg.android.habitica.iap.42gems";
    public static String Purchase84Gems = "com.habitrpg.android.habitica.iap.84gems";

    public static List<String> allGemTypes = Arrays.asList(Purchase4Gems, Purchase21Gems, Purchase42Gems, Purchase84Gems);

    public static String Subscription1Month = "com.habitrpg.android.habitica.subscription.1month";
    public static String Subscription3Month = "com.habitrpg.android.habitica.subscription.3month";
    public static String Subscription6Month = "com.habitrpg.android.habitica.subscription.6month";
    public static String Subscription12Month = "com.habitrpg.android.habitica.subscription.12month";

    public static List<String> allSubscriptionTypes = Arrays.asList(Subscription1Month, Subscription3Month, Subscription6Month, Subscription12Month);

    public static String Subscription1MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.1month";
    public static String Subscription3MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.3month";
    public static String Subscription6MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.6month";
    public static String Subscription12MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.12month";

    public static List<String> allSubscriptionNoRenewTypes = Arrays.asList(Subscription1MonthNoRenew, Subscription3MonthNoRenew, Subscription6MonthNoRenew, Subscription12MonthNoRenew);
}
