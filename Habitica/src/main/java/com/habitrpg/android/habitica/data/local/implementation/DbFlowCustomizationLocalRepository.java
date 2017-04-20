package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.Date;
import java.util.List;

import rx.Observable;

public class DbFlowCustomizationLocalRepository implements CustomizationLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<List<Customization>> getCustomizations(String type, String category) {
        return Observable.defer(() -> {

            Where<Customization> select = new Select()
                    .from(Customization.class)
                    .where(Condition.column("type").eq(type))
                    .and(Condition.CombinedCondition.begin(Condition.column("purchased").eq(true))
                            .or(Condition.column("price").eq(0))
                            .or(Condition.column("price").isNull())
                            .or(Condition.column("isBuyable").eq(true))
                            .or(Condition.column("isBuyable").isNull())
                            .or(Condition.CombinedCondition.begin(
                                    Condition.CombinedCondition.begin(Condition.column("availableUntil").isNull())
                                            .or(Condition.column("availableUntil").greaterThanOrEq(new Date().getTime())))
                                    .and(Condition.CombinedCondition.begin(Condition.column("availableFrom").isNull())
                                            .or(Condition.column("availableFrom").lessThanOrEq(new Date().getTime()))
                                    )
                            )
                    );
            if (category != null) {
                select = select.and(Condition.column("category").eq(category));
            }
            if (type != null && type.equals("background")) {
                select.orderBy(OrderBy.columns("customizationSetName").descending());
            } else {
                select.orderBy(true, "customizationSet");
            }

            return Observable.just(select.queryList());
        });
    }
}
